#!/usr/bin/env perl
use strict;
use warnings;

my $version = 0;

{
	local $^I = '';
	local @ARGV = ('AndroidManifest.xml');
	while (<>) {
		if (($_ =~ m/android:versionCode="(\d{2,})"/) && ($version == 0)) {
			$version = $1+1;
		}
		s/android:versionCode="(\d{2,})"/android:versionCode="$version"/;
		print;
	}
}

{
	local $^I = '';
	local @ARGV = ('res/values/version.xml');
	while (<>) {
		s/(\d{2,})/$version/;
		print;
	}
}
