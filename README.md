# TripTracker

A realtime location sharing app for riders. https://play.google.com/store/apps/details?id=prakash.triptracker

Usefull for bike and car journeys or trekking with friends. Never get lost in a trip anymore and know where your friends are.

You can create a ride group by just entering the trip name . Others can join a ride group by using a unique tripcode.Everyone in the group can know the realtime location of all the other members.

## Features

* Upto one hundrerd people can be part of a ride
* Your location will be updated periodically (currently every 3 minutes)
* Others location will be updated in your map as soon as a new data is available
* Can easily leave the group
* Soft on battery

### Components

* *FusedLocationProider* to get your current location periodically with minimum drain on battery
* *Google Maps Android SDK* to display and update the map
* **Serverless** architecture with all the functions carried out in client side.(More on this below)
* **No-SQL Document** based database is used
* *Firebase anonymous authentication* to uniquely identify each user

![alt text](https://user-images.githubusercontent.com/7611872/44030386-3f3db97c-9f1e-11e8-870f-40b28576cb27.jpg)
![alt text](https://user-images.githubusercontent.com/7611872/44028155-a246e910-9f16-11e8-84e3-7d17bd76d0a4.jpg)

![alt text](https://user-images.githubusercontent.com/7611872/44030483-917b1cb6-9f1e-11e8-9338-e4ae1de1ae55.jpg)
![alt text](https://user-images.githubusercontent.com/7611872/44028159-a36dc138-9f16-11e8-8917-0bc90e0c0864.jpg)

### Serverless Architecture

No application server and only Database server is used. Uses Cloud Firestore Realtime Database which is a **NoSQL Documnet Database**(will soon move to CouchBase Lite) 

Trip creation is handled on client side and a new document is created in cloud Database for every trip created. When new user joins/leaves corresponding changes are made in the document. Location updates are sent to the DB perodically and immediately a callback is triggerd on all the connected clients stating that a new update is available.

#### Work in Progress

1. Move the database layer to **Couchbase Lite with Sync Gateway**
2. Ability to selectively choose members whose location we want to see in realtime. Currently all the ride members' location is shown
3. Create an *admin* functionality and role to perform operations such as removing an user from ride group
