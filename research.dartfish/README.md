Requirements:

* To run, you must have Java SE installed on your computer.

	To install java, google:
	Java SE

	Click on first link, do what it says.

	You do *not* need enterprise java. Just SE is fine.


* You must hook up your data

	Let's say your data is in "/Volumes/MyDataDrive/Dartfish Data"

	Open a terminal (/Applications/Utilities/Terminal)

	go into research.dartfish (which is in where ever you downloaded this github project to)

	run the script "hookup-data" like so:
	./hookup-data "/Volumes/MyDataDrive/Dartfish Data"


Running:


* To clean a previous run, type
	./clean

* To build the source files (only have to do once), type
	./build

* To run, type
	./run

* To archive a run, type
	./archive-run
