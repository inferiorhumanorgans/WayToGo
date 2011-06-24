#!/usr/bin/env ruby

require 'rubygems'
require "rexml/document"
require 'rexml/xpath'
require 'open-uri'
require 'sqlite3'

ROUTES_TABLE =
        "CREATE TABLE routes ( " \
        "name VARCHAR UNIQUE NOT NULL, " \
        "tag VARCHAR UNIQUE NOT NULL, " \
        "number INT UNIQUE NOT NULL," \
        "id VARCHAR UNIQUE NOT NULL," \
        "color VARCHAR NOT NULL, " \
        "start_station VARCHAR, " \
        "end_station VARCHAR);";
STATIONS_TABLE =
        "CREATE TABLE stations ( " \
        "name VARCHAR UNIQUE NOT NULL, " \
        "tag VARCHAR UNIQUE NOT NULL," \
        "address VARCHAR NOT NULL," \
        "lat INTEGER NOT NULL," \
        "lng INTEGER NOT NULL);"

ANDROID_TABLE = "CREATE TABLE android_metadata (locale TEXT);"

def toE6(aFloat)
  return ((aFloat.to_f)*1000000).to_i
end

def createDB
  $db = SQLite3::Database.new( "bart_data.sqlite3" )
  $db.execute ROUTES_TABLE
  $db.execute STATIONS_TABLE
  $db.execute ANDROID_TABLE
  $db.execute "INSERT INTO android_metadata VALUES('en_US')"
  $db.execute "PRAGMA user_version=1;"
end

BART_API_KEY = "MW9S-E7SL-26DU-VV8V"
BART_STATIONS_URL = "http://api.bart.gov/api/stn.aspx?cmd=stns&key="
BART_DETAIL_URL = "http://api.bart.gov/api/stn.aspx?cmd=stninfo&key="
BART_ROUTES_URL = "http://api.bart.gov/api/route.aspx?cmd=routes&key="

def pullStations
  station_file = open(BART_STATIONS_URL + BART_API_KEY)
  station_doc = REXML::Document.new(station_file)
  REXML::XPath.each(station_doc, "/root/stations/station") do |station|
    puts station.elements['abbr'].first.to_s
    bindVars = {}
    bindVars[:name] = station.elements['name'].first.to_s
    bindVars[:tag] = station.elements['abbr'].first.to_s
    bindVars[:address] = "#{station.elements['address'].first.to_s}, #{station.elements['city'].first.to_s}"

    detail_file = open(BART_DETAIL_URL + BART_API_KEY + "&orig=" + bindVars[:tag])
    puts "\t" + BART_DETAIL_URL + BART_API_KEY + "&orig=" + bindVars[:tag]
    detail_doc = REXML::Document.new(detail_file)
    REXML::XPath.each(detail_doc, "/root/stations/station") do |station_detail|
      bindVars[:lat] = toE6(station_detail.elements['gtfs_latitude'].first.to_s)
      bindVars[:lng] = toE6(station_detail.elements['gtfs_longitude'].first.to_s)
    end
    detail_file.close
    
    $db.execute("INSERT INTO stations (name,tag,address,lat,lng) VALUES (:name,:tag,:address,:lat,:lng)", bindVars)
  end
end

def pullRoutes
  station_file = open(BART_ROUTES_URL + BART_API_KEY)
  station_doc = REXML::Document.new(station_file)
  REXML::XPath.each(station_doc, "/root/routes/route") do |route|
    puts route.elements['abbr'].first.to_s
    bindVars = {}
    bindVars[:name] = route.elements['name'].first.to_s
    bindVars[:tag] = route.elements['abbr'].first.to_s
    bindVars[:number] = route.elements['number'].first.to_s.to_i
    bindVars[:id] = route.elements['routeID'].first.to_s
    bindVars[:color] = route.elements['color'].first.to_s

    $db.execute("INSERT INTO ROUTES (name,tag,number,id,color) VALUES (:name,:tag,:number,:id,:color)", bindVars)
  end
end

createDB()
pullStations()
pullRoutes()