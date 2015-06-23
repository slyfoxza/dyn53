Dyn53
=====

Dyn53 is a [Dynamic DNS][] client for [Amazon Route 53][] for use in environments with dynamic external IP address
assignment, such as with home ADSL connections. It uses the [ipify][] web service to discover the external IP address.

Installation
------------

Dyn53 requires a [Java 8][] runtime environment to be installed. To install Dyn53, the `dyn53.jar` and `lib` directory
may be placed anywhere on a filesystem.

Configuration
-------------

Dyn53 requires AWS credentials to update the resource record set in Route 53. This requires the access and secret keys
to be added to a profile named `dyn53` in the `.aws/credentials` file located in the home directory of the user that
Dyn53 will run as.

Running
-------

The command line to execute Dyn53 is of the form:

    java [system properties] -jar dyn53.jar

System properties are specified using the standard Java syntax of `-Dproperty=value`. The following system properties
are supported:

* `net.za.slyfox.dyn53.route53.hostedZoneId` specifies the identifier of the hosted zone containing the resource record
  set to update, as obtained from Route 53. _This property must be specified._
* `net.za.slyfox.dyn53.route53.resourceRecordSetName` specifies the name of the resource record set name to update with
  the external IP address, such as `dynamic.example.com.`. _This property must be specified._
* `net.za.slyfox.dyn53.logFile` specifies the path where Dyn53 should output application logs. The value given in this
  property will be suffixed with the current date, as `.YYYY-mm-dd`. If no value is given for this property, Dyn53 will
  output logs to the process' standard output.
* `net.za.slyfox.dyn53.daemon.pidFile` specifies the path where it should output the PID of its Java process. This
  enables Dyn53 to be run as a daemon with SystemV init scripts. The default behaviour if this value is not specified is
  to not output any PID information.

Compilation
-----------

Dyn53 uses the [Gradle][] build system, and includes the Gradle wrapper to ease bootstrapping builds. To build the
project, simply execute the following command in the root directory of the project:

    ./gradlew build

A more useful command is to build a distribution archive containing the executable JAR and all its dependencies:

    ./gradlew distZip

Executing the `distZip` task will create a ZIP archive in the `build/distributions` directory which may be deployed to
a target host.

License
-------

Dyn53 is licensed under the Apache License, Version 2.0 (the "License"). You may obtain a copy of the License at
<http://www.apache.org/licenses/LICENSE-2.0>.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
language governing permissions and limitations under the License.

Third-party software
--------------------

* [AWS SDK for Java][], Apache License, Version 2.0
* [Guice][], Apache License, Version 2.0
* [JNA][], Apache License, Version 2.0
* [JUnit][], Eclipse Public License v1.0
* [Logback][], Eclipse Public License v1.0
* [Mockito][], MIT License
* [SLF4J][], MIT License

[Amazon Route 53]:  https://aws.amazon.com/route53/
[AWS SDK for Java]: https://aws.amazon.com/sdk-for-java/
[Dynamic DNS]:      https://en.wikipedia.org/wiki/Dynamic_DNS
[Gradle]:           https://gradle.org/
[Guice]:            https://github.com/google/guice
[ipify]:            https://www.ipify.org/
[Java 8]:           https://java.com/en/download/
[JNA]:              https://github.com/twall/jna
[JUnit]:            http://junit.org/
[Logback]:          http://logback.qos.ch/
[Mockito]:          http://mockito.org/
[SLF4J]:            http://www.slf4j.org/