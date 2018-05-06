# Souliss Binding

[Souliss](http://www.souliss.net/) is a networking framework for Arduino and compatibles boards, and is designed to let you easily build a smart home that is distributed over multiple boards via Ethernet, WiFi, wireless point-to-point and RS485 bus. 

Souliss is an open-source and community driven project, you can use the [wiki](https://github.com/souliss/souliss/wiki) and [Community](https://github.com/souliss/souliss/wiki/Community) to get help and share you results.  

## Prerequisites

The binding requires a deployed network.  As a minimum, you need one Souliss node with Ethernet access configured as a [Gateway](https://github.com/souliss/souliss/wiki/Gateway). A Gateway is a special node that is able to communicate with the user interfaces. The binding interacts as a user interface for Souliss.

A starting point is the [Souliss wiki](https://github.com/souliss/souliss/wiki). The best is to start with a single node and connect with SoulissApp. The code for networking activities of this binding is based on [SoulissApp](https://github.com/souliss/souliss/wiki/SoulissApp) code, so once connected with SoulissApp, you can move to openHAB directly.

You can use SoulissApp and the Souliss binding at the same time, and generally up to five (by default, but can be increased) user interfaces simultaneously.

### Sketches

The easiest way is start with a simple example to control an ON/OFF light (though a relay). 
You can go to project [Souliss[(https://github.com/souliss/souliss), see a lot of examples sketches: [Souliss examples](https://github.com/souliss/souliss/tree/friariello/examples)

## Binding Configuration
This binding does not require any special configuration.

## Discovery
This binding can automatically discover devices. First Gateway Node, then Peer Nodes. 

## Supported Things
In Souliss Framework a Typical is one of predefined logic dedicated to smart home devices like lights, heating or antitheft. 

Typical can be one of T11, T12, T13, T14, etc... 

It are defined [here](https://github.com/souliss/souliss/wiki/Typicals).

Typicals match directly with openHAB items. 

That are the supported typicals.

To do consider that not only tipicals are fully tested.
 
|Souliss Typical|Things Name|
|---------|-------------------------------|
|T11|souliss:t11|
|T12|souliss:t12|
|T13|souliss:t13|
|T14|souliss:t14|
|T16|souliss:t16|
|T18|souliss:t18|
|T19|souliss:t19|
|T1A|souliss:t1A|
|T21|souliss:t21|
|T22|souliss:t22|
|T31|souliss:t31|
|T41|souliss:t41|
|T51|souliss:t51|
|T52|souliss:t52|
|T53|souliss:t53|
|T53|souliss:t54|
|T55|souliss:t55|
|T56|souliss:t56|
|T57|souliss:t57|
|T58|souliss:t58|
|T61|souliss:t61|
|T62|souliss:t62|
|T63|souliss:t63|
|T64|souliss:t64|
|T65|souliss:t65|
|T66|souliss:t66|
|T67|souliss:t67|
|T68|souliss:t68|
|Action Message|souliss:topic|


Souliss is a small community and actualy it don't have sufficient human resource to be more active on Openhab official community

These are some very popular forum:

English Group, [here](https://groups.google.com/forum/#!forum/souliss)

Italian Group, [here](https://groups.google.com/forum/#!forum/souliss-it)

Spanish Group, [here] (https://groups.google.com/forum/#!forum/souliss-es)

## Manual Things Configuration

..wiki work in progress..

## Download 

To download latest compiled binding: [here](https://drive.google.com/open?id=0BzYvfLL0ppGAalRxLVhjZ1hXVW8)
