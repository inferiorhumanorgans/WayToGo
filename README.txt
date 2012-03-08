Requiem for a build:

The following dependencies were used unmodified.  More information can be found
at their respective sites:

ACRA:
	<http://code.google.com/p/acra/>
	Apache License 2.0

osmdroid:
	<http://code.google.com/p/osmdroid/>
	Creative Commons 3.0 BY-SA

SLF4J:
	<http://www.slf4j.org/>
	MIT License

Agency logos are optional, but potentially desirable.  If a logo is missing,
the agency's short name will be substituted in its stead.  Currently these are:

actransit.png
caltrain.png
sfmuni.png
bart.png

For BART, additional images are used, if present.  They are named
bart followed by an underscore followed by the hex color code BART
assigns to the route.  As of June 2011, those colors are:

0099cc, 339933, ff0000, ff9933, and ffff33.

All of the images are merely 50x32 PNGs.

Next, ensure that build.xml has the proper path to your Android SDK install.

Then, ensure findbugs is in ~/android/findbugs/, or change build.xml as required.
Findbugs is available here: <http://findbugs.sourceforge.net/> under the GNU LGPL.

If you intend to build an optimized version, ensure that you've changed the
default release key to something other than the debug key.

Last, but not least, if you wish to use precompiled data, check out the scripts in
the utils directory.  BART and NextBus data can be downloaded over the air as needed,
but this is often time consuming.  GTFS data is simply too bulky and disorganized,
so as of yet the database must be precompiled if it's intended to be used.  Note that
Android 2.2 and older cannot handle compressed assets over 1MB.

The generated databases should be placed in the assets/databases/ directory.
