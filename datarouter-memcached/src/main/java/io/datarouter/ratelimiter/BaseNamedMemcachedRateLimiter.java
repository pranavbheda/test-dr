/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.ratelimiter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.datarouter.instrumentation.count.Counters;
import io.datarouter.util.iterable.IterableTool;
import io.datarouter.util.tuple.Pair;

//TODO rolling increases/decreases in limit,
//for spammers who hit the rate limit alot (decrease) and for people/things that are verified as not spam (increase)
public abstract class BaseNamedMemcachedRateLimiter extends NamedRateLimiter{

	private static final String HIT_COUNTER_NAME = "rate limit hit";

	private final long maxAvgRequests;
	private final long maxSpikeRequests;
	private final int numIntervals;
	private final int bucketTimeInterval;
	private final TimeUnit timeunit;

	private final int bucketIntervalMs;
	protected final Duration expiration;

	/**
	 * @param maxAvgRequests     threshold average number of requests
	 * @param maxSpikeRequests   threshold max number of requests
	 * @param numIntervals       number of buckets
	 * @param bucketTimeInterval length of each bucket
	 * @param unit               time unit of bucketTimeInterval
	 */
	public BaseNamedMemcachedRateLimiter(String name, long maxAvgRequests, long maxSpikeRequests, int numIntervals,
			int bucketTimeInterval, TimeUnit unit){
		super(name);
		this.maxAvgRequests = maxAvgRequests;
		this.maxSpikeRequests = maxSpikeRequests;
		this.numIntervals = numIntervals;
		this.bucketIntervalMs = Math.toIntExact(unit.toMillis(bucketTimeInterval));
		this.bucketTimeInterval = bucketTimeInterval;
		this.timeunit = unit;
		this.expiration = Duration.ofMillis(bucketIntervalMs * (numIntervals + 1));
	}

	protected abstract Long increment(String key);
	protected abstract Map<String,Long> readCounts(List<String> keys);

	@Override
	protected Pair<Boolean,Calendar> internalAllow(String key, boolean increment){
		Calendar cal = Calendar.getInstance();
		Map<String,Long> results = readCounts(buildKeysToRead(key, cal));

		String currentMapKey = makeMapKey(key, getTimeStr((Calendar)cal.clone()));
		int total = 0;

		for(Entry<String,Long> entry : results.entrySet()){
			Long numRequests = entry.getValue() == null ? 0L : entry.getValue();
			if(entry.getKey().equals(currentMapKey)){
				numRequests++;
			}

			// exceeded maxSpikeRequests
			if(numRequests > maxSpikeRequests){
				Calendar exceededCal = getDateFromKey(entry.getKey());
				exceededCal.add(Calendar.MILLISECOND, bucketIntervalMs * (numIntervals - 1));
				Counters.inc(HIT_COUNTER_NAME);
				return new Pair<>(false, exceededCal);
			}
			total += numRequests;
		}

		double avgRequests = (double)total / (double)numIntervals;

		// exceeded maxAvgRequests
		if(avgRequests > maxAvgRequests){
			List<Calendar> cals = IterableTool.nullSafeMap(results.keySet(), this::getDateFromKey);
			Calendar lastTime = null;
			for(Calendar calendar : cals){
				if(lastTime == null || calendar.after(lastTime)){
					lastTime = calendar;
				}
			}
			Objects.requireNonNull(lastTime);

			//add to get next available time
			lastTime.add(Calendar.MILLISECOND, bucketIntervalMs);
			Counters.inc(HIT_COUNTER_NAME);
			return new Pair<>(false, lastTime);
		}
		if(increment){
			increment(currentMapKey);
		}
		return new Pair<>(true, null);
	}


	private List<String> buildKeysToRead(String key, Calendar calendar){
		List<String> keys = new ArrayList<>();
		for(int i = 0; i < numIntervals; i++){
			Calendar cal = (Calendar)calendar.clone();

			int amount = i * bucketIntervalMs;
			cal.add(Calendar.MILLISECOND, -amount);

			String mapKey = makeMapKey(key, getTimeStr(cal));
			keys.add(mapKey.toString());
		}
		return keys;
	}

	// makes the key to put in the map from the key given and current time bucket
	private String makeMapKey(String key, String time){
		return key.replaceAll("!", "%21") + "!" + time;
	}

	// inverse of makeMapKey
	private Pair<String,String> unmakeMapKey(String mapKey){
		String[] splits = mapKey.split("!");
		return new Pair<>(splits[0].replaceAll("%21", "!"), splits[1]);
	}


	/*
	 * returns a string of the time bucket closest to (and below) the given calendar
	 * ie:
	 *   2009-06-06 11:11:11.123 => 20090606111110 when timeUnit = seconds and bucketInterval = 10s
	 *   						 => 2009060606     when timeUnit = hours   and bucketInterval = 6 hours
	 *   						 => 200906061108   when timeUnit = minutes and bucketInterval = 4 minutes
	 */
	protected String getTimeStr(Calendar cal){
		int calendarField;
		switch(timeunit){
		case DAYS:
			calendarField = Calendar.DATE;
			break;
		case HOURS:
			calendarField = Calendar.HOUR;
			break;
		case MINUTES:
			calendarField = Calendar.MINUTE;
			break;
		case SECONDS:
			calendarField = Calendar.SECOND;
			break;
		default:
			calendarField = Calendar.MILLISECOND;
			break;
		}
		setCalendarFieldForBucket(cal, calendarField, bucketTimeInterval);
		return getDateFormatForTimeUnit().format(cal.getTime());
	}

	//gets a minimum date format for the current timeUnit
	private DateFormat getDateFormatForTimeUnit(){
		switch(timeunit){
		case DAYS:
			return new SimpleDateFormat("yyyyMMdd");
		case HOURS:
			return new SimpleDateFormat("yyyyMMddHH");
		case MINUTES:
			return new SimpleDateFormat("yyyyMMddHHmm");
		case SECONDS:
			return new SimpleDateFormat("yyyyMMddHHmmss");
		default:
			return new SimpleDateFormat("yyyyMMddHHmmssSSS"); //MILLISECONDS
		}
	}

	private Calendar getDateFromKey(String key){
		String dateString = unmakeMapKey(key).getRight();
		try{
			DateFormat dateFormat = getDateFormatForTimeUnit();
			Calendar cal = Calendar.getInstance();
			cal.setTime(dateFormat.parse(dateString));
			return cal;
		}catch(ParseException e){
			throw new IllegalStateException("unparseable key " + key, e);
		}
	}

 	// rely on int rounding to truncate. 10*(x/10) gives closet multiple of 10 below x
	private static void setCalendarFieldForBucket(Calendar calendar, int calendarField, int fieldInterval){
		calendar.set(calendarField, fieldInterval * (calendar.get(calendarField) / fieldInterval));
	}

	public long getMaxAvgRequests(){
		return maxAvgRequests;
	}

	public long getMaxSpikeRequests(){
		return maxSpikeRequests;
	}

	public int getNumIntervals(){
		return numIntervals;
	}

}