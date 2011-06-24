#!/usr/bin/env ruby

require 'rubygems'
require 'sqlite3'
require 'fastercsv'

$agency = ARGV[0]
puts "Currently selected agency: #{$agency}"
$KCODE='u'

ROUTES_TABLE =
          "CREATE TABLE routes ( " \
          "route_id VARCHAR UNIQUE NOT NULL, " \
          "agency_id VARCHAR, " \
          "route_short_name VARCHAR NOT NULL, " \
          "route_long_name VARCHAR NOT NULL, " \
          "route_desc VARCHAR, " \
          "route_type INTEGER NOT NULL, " \
          "route_url VARCHAR, " \
          "route_color VARCHAR DEFAULT 'FFFFFF', " \
          "route_text_color VARCHAR DEFAULT '000000');"

STOPS_TABLE =
          "CREATE TABLE stops ( " \
          "stop_id VARCHAR UNIQUE NOT NULL, " \
          "stop_code VARCHAR UNIQUE, " \
          "stop_name VARCHAR NOT NULL, " \
          "stop_desc VARCHAR, " \
          "lat INTEGER NOT NULL, " \
          "lng INTEGER NOT NULL, " \
          "zone_id VARCHAR," \
          "stop_url VARCHAR, " \
          "location_type INTEGER, " \
          "parent_station VARCHAR);"

CALENDARS_TABLE =
          "CREATE TABLE calendars ( " \
          "service_id VARCHAR NOT NULL, " \
          "monday BOOLEAN NOT NULL, " \
          "tuesday BOOLEAN NOT NULL, " \
          "wednesday BOOLEAN NOT NULL, " \
          "thursday BOOLEAN NOT NULL, " \
          "friday BOOLEAN NOT NULL, " \
          "saturday BOOLEAN NOT NULL, " \
          "sunday BOOLEAN NOT NULL, " \
          "start_date INTEGER NOT NULL, " \
          "end_date INTEGER NOT NULL); "

TRIPS_TABLE =
          "CREATE TABLE trips ( " \
          "route_id VARCHAR NOT NULL, " \
          "service_id VARCHAR NOT NULL, " \
          "trip_id VARCHAR NOT NULL, " \
          "trip_headsign VARCHAR, " \
          "trip_short_name VARCHAR, " \
          "direction_id INTEGER, " \
          "block_id VARCHAR, " \
          "shape_id VARCHAR);"

STOP_TIMES_TABLE =
          "CREATE TABLE stop_times ( " \
          "trip_id VARCHAR NOT NULL, " \
          "arrival_time VARCHAR NOT NULL, " \
          "departure_time VARCHAR NOT NULL, " \
          "stop_id VARCHAR NOT NULL, " \
          "stop_sequence INTEGER NOT NULL, " \
          "stop_headsign VARCHAR, " \
          "pickup_type INTEGER, " \
          "drop_off_type INTEGER, " \
          "shape_dist_traveled NUMERIC);"

ANDROID_TABLE = "CREATE TABLE android_metadata (locale TEXT);"

def createDB
  $db = SQLite3::Database.new( "gtfs_data_#{$agency.gsub('-', '_')}.sqlite3" )
  $db.execute ROUTES_TABLE
  $db.execute STOPS_TABLE
  $db.execute STOP_TIMES_TABLE
  $db.execute CALENDARS_TABLE
  $db.execute TRIPS_TABLE
  $db.execute ANDROID_TABLE
  $db.execute "INSERT INTO android_metadata VALUES('en_US')"
  $db.execute "PRAGMA user_version=1;"
end

def parseRoutes()
  FasterCSV.foreach("#{$agency}/routes.txt", :headers => :first_row, :header_converters => :symbol, :encoding => 'u') do |row|

    if row[:route_short_name].nil?
      row[:route_short_name] = row[:route_long_name]
    end

    cols = []
    bind_cols = []
    values = {}

    the_hash = row.to_hash
    row.to_hash.each do |k,v|
      unless v.nil?
        cols.push k.to_s
        bind_cols.push ":#{k.to_s}"
        values[k] = v
      end
    end

    $db.execute("INSERT INTO routes (#{cols.join(',')}) VALUES (#{bind_cols.join(',')});", values)
  end
end

def parseStops()
  FasterCSV.foreach("#{$agency}/stops.txt", :headers => :first_row, :header_converters => :symbol, :encoding => 'u') do |row|

    cols = []
    bind_cols = []
    values = {}

    the_hash = row.to_hash
    row.to_hash.each do |k,v|
      unless v.nil?
        tag = k.to_s

        if tag == 'stop_lon'
          cols.push 'lng'
          values[k] = (v.to_f*1000000).to_i
        elsif tag == 'stop_lat'
          cols.push 'lat'
          values[k] = (v.to_f*1000000).to_i
        else
          cols.push k.to_s
          values[k] = v
        end
        bind_cols.push ":#{k.to_s}"

      end
    end

    $db.execute("INSERT INTO stops (#{cols.join(',')}) VALUES (#{bind_cols.join(',')});", values)
  end
end

def parseCalendars()
  FasterCSV.foreach("#{$agency}/calendar.txt", :headers => :first_row, :header_converters => :symbol, :encoding => 'u') do |row|

    cols = []
    bind_cols = []
    values = {}

    the_hash = row.to_hash
    row.to_hash.each do |k,v|
      unless v.nil?
        cols.push k.to_s
        values[k] = v
        bind_cols.push ":#{k.to_s}"
      end
    end

    $db.execute("INSERT INTO calendars (#{cols.join(',')}) VALUES (#{bind_cols.join(',')});", values)
  end
end

def parseTrips()
  FasterCSV.foreach("#{$agency}/trips.txt", :headers => :first_row, :header_converters => :symbol, :encoding => 'u') do |row|

    cols = []
    bind_cols = []
    values = {}

    the_hash = row.to_hash
    row.to_hash.each do |k,v|
      unless v.nil?
        cols.push k.to_s
        values[k] = v
        bind_cols.push ":#{k.to_s}"
      end
    end

    $db.execute("INSERT INTO trips (#{cols.join(',')}) VALUES (#{bind_cols.join(',')});", values)
  end
end

def parseStopTimes()
  FasterCSV.foreach("#{$agency}/stop_times.txt", :headers => :first_row, :header_converters => :symbol, :encoding => 'u') do |row|

    cols = []
    bind_cols = []
    values = {}

    the_hash = row.to_hash
    row.to_hash.each do |k,v|
      unless v.nil?
        cols.push k.to_s
        values[k] = v
        bind_cols.push ":#{k.to_s}"
      end
    end

    $db.execute("INSERT INTO stop_times (#{cols.join(',')}) VALUES (#{bind_cols.join(',')});", values)
  end
end

createDB()
parseRoutes()
parseStops()
parseStopTimes()
parseCalendars()
parseTrips()