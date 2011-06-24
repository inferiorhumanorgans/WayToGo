#!/usr/bin/env ruby

require 'rubygems'
require "rexml/document"
require 'rexml/xpath'
require 'open-uri'
require 'sqlite3'

# http://stackoverflow.com/questions/3707797/where-does-android-store-sqlite-database-version

ROUTES_TABLE =
          "CREATE TABLE routes ( " \
          "tag VARCHAR UNIQUE NOT NULL, " \
          "title VARCHAR NOT NULL, " \
          "lat_min INTEGER, " \
          "lat_max INTEGER, " \
          "lng_min INTEGER, " \
          "lng_max INTEGER, " \
          "color VARCHAR NOT NULL);"
DIRECTIONS_TABLE =
          "CREATE TABLE directions ( " \
          "route_tag VARCHAR NOT NULL, " \
          "tag VARCHAR UNIQUE NOT NULL," \
          "title VARCHAR NOT NULL, " \
          "visible BOOLEAN NOT NULL, " \
          "direction VARCHAR NOT NULL);"
DIRECTIONS_STOPS_TABLE =
          "CREATE TABLE directions_stops ( " \
          "direction_tag VARCHAR NOT NULL, " \
          "stop_tag VARCHAR NOT NULL," \
          "position INT NOT NULL);"
STOPS_TABLE =
          "CREATE TABLE stops ( " \
          "tag VARCHAR UNIQUE NOT NULL, " \
          "stop_id INTEGER UNIQUE NOT NULL, " \
          "title VARCHAR NOT NULL," \
          "lat INTEGER NOT NULL, " \
          "lng INTEGER NOT NULL);"
ROUTES_STOPS_TABLE =
          "CREATE TABLE routes_stops ( " \
          "route_tag VARCHAR NOT NULL, " \
          "stop_tag VARCHAR NOT NULL);"

EXTRA_INDEX_1 = "create index ds_dt_idx on directions_stops(direction_tag);"

ANDROID_TABLE = "CREATE TABLE android_metadata (locale TEXT);"

$agency = ARGV[0]
$route_tags = []
puts "Currently selected agency: #{$agency}"

def toE6(aFloat)
	return ((aFloat.to_f)*1000000).to_i
end

def pullRouteConfig(theRouteTag)
  puts "Fetching RouteConfig for #{theRouteTag}"
  rc_file = open("http://webservices.nextbus.com/service/publicXMLFeed?command=routeConfig&terse&verbose&a=#{$agency}&r=#{theRouteTag.gsub(' ', '%20')}")
  doc = REXML::Document.new rc_file
  REXML::XPath.each(doc, '/body/route') do |route|
    bind_vars = {
      :tag => route.attributes['tag'],
      :title => route.attributes['title'],
      :lat_min => toE6(route.attributes['latMin']),
      :lat_max => toE6(route.attributes['latMax']),
      :lng_min => toE6(route.attributes['lonMin']),
      :lng_max => toE6(route.attributes['lonMax']),
      :color => route.attributes['color']
    }
    $db.execute("INSERT INTO routes (tag,title,lat_min,lat_max,lng_min,lng_max,color) VALUES(:tag,:title,:lat_min,:lat_max,:lng_min,:lng_max,:color)", bind_vars)
    REXML::XPath.each(route, './stop') do |stop|
      stop_bind_vars = {
        :tag => stop.attributes['tag'],
        :title => stop.attributes['title'],
        :stop_id => stop.attributes['stopId'],
        :lat => toE6(stop.attributes['lat']),
        :lng => toE6(stop.attributes['lon'])
      }
      # 500 000 000
      # 301,701,020
      stop_bind_vars[:stop_id] ||= stop_bind_vars[:tag] #+ (500000000 + rand(900000000)).to_s
      begin
        $db.execute("INSERT INTO stops (tag,title,stop_id,lat,lng) VALUES (:tag,:title,:stop_id,:lat,:lng)", stop_bind_vars)
      rescue SQLite3::SQLException => e
        # puts "Ignoring #{e.inspect}"
      end
      $db.execute("INSERT INTO routes_stops (route_tag,stop_tag) VALUES (:route_tag,:stop_tag)", {:route_tag => route.attributes['tag'], :stop_tag => stop.attributes['tag']})
    end
    REXML::XPath.each(route, './direction') do |dir|
      dir_bind_vars = {
        :tag => dir.attributes['tag'],
        :title => dir.attributes['title'],
        :direction => dir.attributes['name'],
        :route_tag => route.attributes['tag'],
        :visible => (dir.attributes['useForUI'] == 'true') ? 1 : 0,
      }
      $db.execute("INSERT INTO directions (tag,title,direction,route_tag,visible) VALUES (:tag,:title,:direction,:route_tag,:visible)", dir_bind_vars)
      i = 0
      REXML::XPath.each(dir, './stop') do |stop|
        ds_bind_vars = {
          :direction_tag => dir.attributes['tag'],
          :stop_tag => stop.attributes['tag'],
          :position => i
        }
        $db.execute("INSERT INTO directions_stops (direction_tag,stop_tag,position) VALUES (:direction_tag,:stop_tag,:position)", ds_bind_vars)
        i += 1
      end
    end
  end
end

def pullRouteList
  route_list_file = open("http://webservices.nextbus.com/service/publicXMLFeed?command=routeList&a=#{$agency}")
  route_list_doc = REXML::Document.new route_list_file
  REXML::XPath.each(route_list_doc, "/body/route") {|route|
    $route_tags.push route.attributes['tag']
  }
end

def createDB
  $db = SQLite3::Database.new( "nextbus_data_#{$agency.gsub('-', '_')}.sqlite3" )
  $db.execute ROUTES_TABLE
  $db.execute DIRECTIONS_TABLE
  $db.execute DIRECTIONS_STOPS_TABLE
  $db.execute STOPS_TABLE
  $db.execute ROUTES_STOPS_TABLE
  $db.execute EXTRA_INDEX_1
  $db.execute ANDROID_TABLE
  $db.execute "INSERT INTO android_metadata VALUES('en_US')"
  $db.execute "PRAGMA user_version=1;"
end

createDB()
pullRouteList()
$route_tags.each do |tag|
  $db.transaction
  pullRouteConfig(tag)
  $db.commit
end
# pullRouteConfig('DB')

# parseRoute(route_tags.first)
