VisITMeta
=========
VisITMeta is an *experimental* [IF-MAP][1] 2.0/2.1 compliant MAP client written in Java
that visualizes metadata stored on a MAP server.
It supports features like showing a history of all metadata stored on a MAPS, using
filter and search techniques to navigate the metadata as well as some visualization
techniques like animations, highlighting and so on.
Development was done by [Hochschule Hannover (University of Applied Sciences and Arts, 
Hanover)][2] within the [VisITMeta research project][3], (support code 17PNT032) which is 
funded by the german [BMBF (Federal Ministry of Education and Research)][8].

VisITMeta input device: LeapMotion
==================================
This project adds support for the LeapMotion input device controller to the VisITMeta
GUI.
It uses functionality and methods from the SDK v1.

Building
========
This section describes, how to build LeapMotion for VisITMeta from scratch.

Prerequisites
-------------
In order to build VisITMeta with Maven you need to install
[Maven 3][4] manually or via the package manager of your
operating system.

Furthermore, you need to download the [LeapMotion SDK v1][13] suitable to your
platform and operating system.

Extract the SDK archive into a folder of your choice.

Copying files form the SDK
--------------------------

Go to the `src/main/templates/native-libs/` folder inside this project (where
this README lies).

There should exist three folders named `linux`, `osx` and `windows`.
Switch to the one suitable for your operating system and choose the subdirectory for your
system architecture (`x64` or `x86`).

*Example:*
If you are on a 64bit Linux system, the folder
`src/main/templates/native-libs/linux/x64/` would be the correct one.

Now, go to the the LeapMotion SDK folder you extracted earlier and go to
`LeapDeveloperKit/LeapSDK/lib`.

Depending on your system, you have to copy the following files:

**Linux**

*	`libLeap.so` and `libLeapJava.so` from the `x64` or `x86` folder to
	`native-libs/linux/x64` or `native-libs/linux/x86`
*	`LeapJava.jar` to `native-libs/`

**Windows**

* the `x64` or `x86` folder to `native-libs/windows/`
* `LeapJava.jar` to `native-libs/` 

**Mac OSX**

* `libLeap.dylib` and `libLeapJava.dylib` to `native-libs/osx/x64`
* `LeapJava.jar` to `native-libs/`

Installing LeapMotion for Java into local Maven repository
----------------------------------------------------------

Afterwards you have to edit the script file for your system under 
`src/main/templates/installation` to reflect the correct filename and version of the
loaded SDK version.
	
	#!/bin/bash
	mvn org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file \
		-Dfile=../native-libs/LeapJava.jar \
		-DgroupId=com.leapmotion.leap -DartifactId=leapmotion \
		-Dversion=1.2.0 -Dpackaging=jar

Furthermore the `pom.xml` in the projects root directory has to be adapted to that version
number in the section
	
	<dependency>
		<groupId>com.leapmotion.leap</groupId>
		<artifactId>leapmotion</artifactId>
		<version>1.2.0</version>
	</dependency>

Build LeapMotion for VisITMeta
------------------------------
Now you can build the LeapMotion for VisITMeta-module by executing:

    $ mvn package

in the root directory of the LeapMotion for VisITMeta project (the directory
containing this `README` file). Maven should download all further
needed dependencies for you.
After a successful build you should find a zip-archive called
`visitmeta-device-leapmotion-<version>-bundle.zip` in 
`target`.

Copy this archive to the `devices` subfolder of your already build VisITMeta folder and
extract it.

Configuration
=============
This section describes the configuration options of the LeapMotion for VisITMeta module.

Running
=======
Start the VisITMeta GUI via

	$ sh start-visualization.sh

The log output should tell you that the LeapMotion controller was loaded, unless there
was an error.

Just select a running connection from the treeview on the left side and try to use the
LeapMotion controller to navigate/move the graph.


Feedback
========
If you have any questions, problems or comments, please contact
	trust@f4-i.fh-hannover.de

License
=======
VisITMeta is licensed under the [Apache License, Version 2.0][7].
The visualization component uses the Java Swing Range Slider source code from [Ernie You][10] ([Github][11]).
The corresponding license is provided with the file LICENSE-swingRangeSlider.txt in the
root-directory of `visitmeta-distribution`.

----

[1]: http://www.trustedcomputinggroup.org/resources/tnc_ifmap_binding_for_soap_specification
[2]: http://www.hs-hannover.de/start/index.html
[3]: http://trust.f4.hs-hannover.de/projects/visitmeta.html
[4]: https://maven.apache.org/download.html
[5]: https://github.com/trustathsh/irondemo.git
[6]: https://github.com/trustathsh/irond.git
[7]: http://www.apache.org/licenses/LICENSE-2.0.html
[8]: http://www.bmbf.de/en/index.php
[9]: http://www.neo4j.org/
[10]: http://ernienotes.wordpress.com/2010/12/27/creating-a-java-swing-range-slider/
[11]: https://github.com/ernieyu/Swing-range-slider
[12]: http://trust.f4.hs-hannover.de
[13]: https://developer.leapmotion.com/v1