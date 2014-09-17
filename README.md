Cyclo
=====

Cyclo Location Recording Service


What is?
--------

Cyclo is a SDK contains a service which does record current location.

Any app can use CycloService for recording location by simply using CycloConnector class.

CycloService is a Messenger Service, so you don't need to configure complexly.

How to?
-------

1. Build this project by Android Studio.
1. Install app.(Dashboard)
1. Copy CycloConnector.java file to your project.
1. Use it in your activity. (See DashboardActivity.java)
1. You can control the service with CycloConnector.
1. You can receive locations through Broadcast Receiver.
1. You can also receive status and locations through CycloConnector.StatusListener class

To do
-----

* Gathering location without huge bettery drain.
* Calculate speed and distance and duration.
* Detect break-time.

I need your help.
