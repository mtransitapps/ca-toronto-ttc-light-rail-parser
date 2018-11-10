package org.mtransit.parser.ca_toronto_ttc_light_rail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.mtransit.parser.CleanUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.Pair;
import org.mtransit.parser.SplitUtils;
import org.mtransit.parser.Utils;
import org.mtransit.parser.SplitUtils.RouteTripSpec;
import org.mtransit.parser.gtfs.data.GCalendar;
import org.mtransit.parser.gtfs.data.GCalendarDate;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GSpec;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.gtfs.data.GTripStop;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MDirectionType;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.mt.data.MTrip;
import org.mtransit.parser.mt.data.MTripStop;

// http://www1.toronto.ca/wps/portal/contentonly?vgnextoid=96f236899e02b210VgnVCM1000003dd60f89RCRD
// http://opendata.toronto.ca/TTC/routes/OpenData_TTC_Schedules.zip
public class TorontoTTCLightRailAgencyTools extends DefaultAgencyTools {

	public static void main(String[] args) {
		if (args == null || args.length == 0) {
			args = new String[3];
			args[0] = "input/gtfs.zip";
			args[1] = "../../mtransitapps/ca-toronto-ttc-light-rail-android/res/raw/";
			args[2] = ""; // files-prefix
		}
		new TorontoTTCLightRailAgencyTools().start(args);
	}

	private HashSet<String> serviceIds;

	@Override
	public void start(String[] args) {
		System.out.printf("\nGenerating TTC light rail data...");
		long start = System.currentTimeMillis();
		this.serviceIds = extractUsefulServiceIds(args, this);
		super.start(args);
		System.out.printf("\nGenerating TTC light rail data... DONE in %s.", Utils.getPrettyDuration(System.currentTimeMillis() - start));
	}

	@Override
	public boolean excludingAll() {
		return this.serviceIds != null && this.serviceIds.isEmpty();
	}

	@Override
	public boolean excludeCalendar(GCalendar gCalendar) {
		if (this.serviceIds != null) {
			return excludeUselessCalendar(gCalendar, this.serviceIds);
		}
		return super.excludeCalendar(gCalendar);
	}

	@Override
	public boolean excludeCalendarDate(GCalendarDate gCalendarDates) {
		if (this.serviceIds != null) {
			return excludeUselessCalendarDate(gCalendarDates, this.serviceIds);
		}
		return super.excludeCalendarDate(gCalendarDates);
	}

	@Override
	public boolean excludeRoute(GRoute gRoute) {
		return super.excludeRoute(gRoute);
	}

	@Override
	public boolean excludeTrip(GTrip gTrip) {
		if (this.serviceIds != null) {
			return excludeUselessTrip(gTrip, this.serviceIds);
		}
		return super.excludeTrip(gTrip);
	}

	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_LIGHT_RAIL;
	}

	@Override
	public long getRouteId(GRoute gRoute) {
		return Long.parseLong(gRoute.getRouteShortName()); // using route short name as route ID
	}

	@Override
	public String getRouteLongName(GRoute gRoute) {
		return cleanRouteLongName(gRoute);
	}

	private String cleanRouteLongName(GRoute gRoute) {
		String routeLongName = gRoute.getRouteLongName();
		routeLongName = routeLongName.toLowerCase(Locale.ENGLISH);
		return CleanUtils.cleanLabel(routeLongName);
	}

	private static final String AGENCY_COLOR = "B80000"; // RED (AGENCY WEB SITE CSS)

	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	private static final String COLOR_00529F = "00529F"; // BLUE (NIGHT BUSES)

	@Override
	public String getRouteColor(GRoute gRoute) {
		int rsn = Integer.parseInt(gRoute.getRouteShortName());
		if (rsn >= 300 && rsn <= 399) { // Night Network
			return COLOR_00529F;
		}
		return null; // use agency color instead of provided colors (like web site)
	}

	private static HashMap<Long, RouteTripSpec> ALL_ROUTE_TRIPS2;
	static {
		HashMap<Long, RouteTripSpec> map2 = new HashMap<Long, RouteTripSpec>();
		map2.put(506L, new RouteTripSpec(506L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.EAST.getId(), // MAIN STREET STATION
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.WEST.getId()) // HIGH PARK LOOP
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						"5292", // HIGH PARK LOOP <=
								"8763", // HOWARD PARK AVE AT RONCESVALLES AVE
								"8999", // != HOWARD PARK AVE AT DUNDAS ST WEST
								"9132", // != HOWARD PARK AVE AT RONCESVALLES AVE <=
								"2954", // != DUNDAS ST WEST AT HOWARD PARK AVE
								"2243", // == DUNDAS ST WEST AT SORAUREN AVE
								"7506", // DUNDAS ST WEST AT STERLING RD
								"3797", // GERRARD ST EAST AT COXWELL AVE
								"8980", // COXWELL AVE AT UPPER GERRARD ST EAST
								"14260", // MAIN STREET STATION
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"14260", // MAIN STREET STATION
								"10283", // COXWELL AVE AT LOWER GERRARD ST EAST
								"2048", // GERRARD ST EAST AT ASHDALE AVE
								"8135", // COLLEGE ST AT LANSDOWNE AVE
								"9132", // HOWARD PARK AVE AT RONCESVALLES AVE
								"5292", // HIGH PARK LOOP
						})) //
				.compileBothTripSort());
		map2.put(510L, new RouteTripSpec(510L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.NORTH.getId(), // SPADINA STATION
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.SOUTH.getId()) // UNION STATION
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"9227", // UNION STATION <=
								"6075", // != SPADINA AVE AT QUEENS QUAY WEST NORTH SIDE
								"478", // != <> QUEENS QUAY LOOP AT LOWER SPADINA AVE <=
								"9243", // == SPADINA AVE AT BREMNER BLVD NORTH SIDE
								"5275", // == != SPADINA AVE AT KING ST WEST NORTH SIDE
								"8346", // != SPADINA AVE AT RICHMOND ST WEST
								"7582", // SPADINA AVE AT QUEEN ST WEST NORTH SIDE
								"14339", // SPADINA STATION =>
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"14339", // SPADINA STATION
								"9203", // == != SPADINA AVE AT QUEEN ST WEST SOUTH SIDE
								"10089", // != <> CHARLOTTE ST AT OXLEY ST =>
								"10138", // != SPADINA AVE AT KING ST WEST
								"6639", // SPADINA AVE AT BREMNER BLVD
								"2125", // == != QUEENS QUAY WEST AT LOWER SPADINA AVE EAST SIDE
								"478", // != <> QUEENS QUAY LOOP AT LOWER SPADINA AVE =>
								"15122", // != QUEENS QUAY W AT REES ST
								"9227", // UNION STATION =>
						})) //
				.compileBothTripSort());
		ALL_ROUTE_TRIPS2 = map2;
	}

	@Override
	public int compareEarly(long routeId, List<MTripStop> list1, List<MTripStop> list2, MTripStop ts1, MTripStop ts2, GStop ts1GStop, GStop ts2GStop) {
		if (ALL_ROUTE_TRIPS2.containsKey(routeId)) {
			return ALL_ROUTE_TRIPS2.get(routeId).compare(routeId, list1, list2, ts1, ts2, ts1GStop, ts2GStop, this);
		}
		return super.compareEarly(routeId, list1, list2, ts1, ts2, ts1GStop, ts2GStop);
	}

	@Override
	public ArrayList<MTrip> splitTrip(MRoute mRoute, GTrip gTrip, GSpec gtfs) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return ALL_ROUTE_TRIPS2.get(mRoute.getId()).getAllTrips();
		}
		return super.splitTrip(mRoute, gTrip, gtfs);
	}

	@Override
	public Pair<Long[], Integer[]> splitTripStop(MRoute mRoute, GTrip gTrip, GTripStop gTripStop, ArrayList<MTrip> splitTrips, GSpec routeGTFS) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return SplitUtils.splitTripStop(mRoute, gTrip, gTripStop, routeGTFS, ALL_ROUTE_TRIPS2.get(mRoute.getId()), this);
		}
		return super.splitTripStop(mRoute, gTrip, gTripStop, splitTrips, routeGTFS);
	}

	private static final String WEST = "west";
	private static final String EAST = "east";
	private static final String SOUTH = "south";
	private static final String NORTH = "north";

	@Override
	public void setTripHeadsign(MRoute mRoute, MTrip mTrip, GTrip gTrip, GSpec gtfs) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return; // split
		}
		String gTripHeadsignLC = gTrip.getTripHeadsign().toLowerCase(Locale.ENGLISH);
		if (gTripHeadsignLC.startsWith(EAST)) {
			mTrip.setHeadsignDirection(MDirectionType.EAST);
			return;
		} else if (gTripHeadsignLC.startsWith(WEST)) {
			mTrip.setHeadsignDirection(MDirectionType.WEST);
			return;
		} else if (gTripHeadsignLC.startsWith(NORTH)) {
			mTrip.setHeadsignDirection(MDirectionType.NORTH);
			return;
		} else if (gTripHeadsignLC.startsWith(SOUTH)) {
			mTrip.setHeadsignDirection(MDirectionType.SOUTH);
			return;
		}
		System.out.printf("\n%s: Unexpected trip %s!\n", mRoute.getId(), gTrip);
		System.exit(-1);
	}

	@Override
	public String cleanTripHeadsign(String tripHeadsign) {
		tripHeadsign = tripHeadsign.toLowerCase(Locale.ENGLISH);
		tripHeadsign = CleanUtils.CLEAN_AT.matcher(tripHeadsign).replaceAll(CleanUtils.CLEAN_AT_REPLACEMENT);
		tripHeadsign = CleanUtils.CLEAN_AND.matcher(tripHeadsign).replaceAll(CleanUtils.CLEAN_AND_REPLACEMENT);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		tripHeadsign = CleanUtils.cleanNumbers(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	@Override
	public boolean mergeHeadsign(MTrip mTrip, MTrip mTripToMerge) {
		System.out.printf("\nUnexptected trips to merge %s & %s!\n", mTrip, mTripToMerge);
		System.exit(-1);
		return false;
	}

	private static final Pattern SIDE = Pattern.compile("((^|\\W){1}(side)(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String SIDE_REPLACEMENT = "$2$4";

	private static final Pattern EAST_ = Pattern.compile("((^|\\W){1}(east)(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String EAST_REPLACEMENT = "$2E$4";

	private static final Pattern WEST_ = Pattern.compile("((^|\\W){1}(west)(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String WEST_REPLACEMENT = "$2W$4";

	private static final Pattern NORTH_ = Pattern.compile("((^|\\W){1}(north)(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String NORTH_REPLACEMENT = "$2N$4";

	private static final Pattern SOUTH_ = Pattern.compile("((^|\\W){1}(south)(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String SOUTH_REPLACEMENT = "$2S$4";

	@Override
	public String cleanStopName(String gStopName) {
		gStopName = gStopName.toLowerCase(Locale.ENGLISH);
		gStopName = CleanUtils.CLEAN_AT.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AT_REPLACEMENT);
		gStopName = SIDE.matcher(gStopName).replaceAll(SIDE_REPLACEMENT);
		gStopName = EAST_.matcher(gStopName).replaceAll(EAST_REPLACEMENT);
		gStopName = WEST_.matcher(gStopName).replaceAll(WEST_REPLACEMENT);
		gStopName = NORTH_.matcher(gStopName).replaceAll(NORTH_REPLACEMENT);
		gStopName = SOUTH_.matcher(gStopName).replaceAll(SOUTH_REPLACEMENT);
		gStopName = CleanUtils.removePoints(gStopName);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		gStopName = CleanUtils.cleanNumbers(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}
}
