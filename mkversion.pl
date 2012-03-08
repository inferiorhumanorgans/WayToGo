#!/usr/bin/env perl
use strict;
use warnings;

my $version = 0;


open(VERSION_FILE, "+<version.txt");
$version = <VERSION_FILE>+1;
truncate(VERSION_FILE, 0);
seek(VERSION_FILE, 0, 0);
print VERSION_FILE $version . "\n";
close(VERSION_FILE);

{
	open(INFILE, "<versioning/AndroidManifest.xml.in");
	open(OUTFILE, ">AndroidManifest.xml");

	while (<INFILE>) {
		s/###/$version/g;
		print OUTFILE $_;
	}
	close(INFILE);
	close(OUTFILE);
}

{
	open(INFILE, "<versioning/version.xml.in");
	open(OUTFILE, ">res/values/version.xml");

	while (<INFILE>) {
		s/###/$version/g;
		print OUTFILE $_;
	}
	close(INFILE);
	close(OUTFILE);
}
