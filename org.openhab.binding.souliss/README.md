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

Typicals match directly with openHAB Thing type.

|Device type |Typical Code | Thing type | 
|------------|---------|-------------------------------|
|ON/OFF Digital Output with Timer Option|T11|souliss:t11|
|ON/OFF Digital Output with AUTO mode|T12|souliss:t12|
|Digital Input Value|T13|souliss:t13|
|Pulse Digital Output|T14|souliss:t14|
|RGB LED Strip|T16|souliss:t16|
|ON/OFF Digital Output|T18|souliss:t18|
|Single Color LED Strip|T19|souliss:t19|
|Digital Input Pass Through|T1A|souliss:t1A|
|Motorized devices with limit switches|T21|souliss:t21|
|Motorized devices with limit switches and middle position|T22|souliss:t22|
|Temperature control|T31|souliss:t31|
|Anti-theft integration (Main)|T41|souliss:t41|
|Analog input, half-precision floating point|T51|souliss:t51|
|Temperature measure (-20, +50) °C|T52|souliss:t52|
|Humidity measure (0, 100) %|T53|souliss:t53|
|Light Sensor (0, 40) kLux|T54|souliss:t54|
|Voltage (0, 400) V|T55|souliss:t55|
|Current (0, 25)  A|T56|souliss:t56|
|Power (0, 6500)  W|T57|souliss:t57|
|Pressure measure (0, 1500) hPa|T58|souliss:t58|
|Analog Setpoint|T61|souliss:t61|
|Analog Setpoint-Temperature measure (-20, +50) °C|T62|souliss:t62|
|Analog Setpoint-Humidity measure (0, 100) %|T63|souliss:t63|
|Analog Setpoint-Light Sensor (0, 40) kLux|T64|souliss:t64|
|Analog Setpoint-Voltage (0, 400) V|T65|souliss:t65|
|Analog Setpoint-Current (0, 25)  A|T66|souliss:t66|
|Analog Setpoint-Power (0, 6500)  W|T67|souliss:t67|
|Analog Setpoint-Pressure measure (0, 1500) hPa|T68|souliss:t68|
|Broadcast messages|Action Message|souliss:topic|


The following matrix lists the capabilities (channels) for each type:

|Thing type |onoff | sleep | lastStatusStored | healty |automode|stateOnOff|stateOpenClose|pulse|whitemode|roller_brightness|dimmer_brightness|ledcolor|one|two|three|four|five|six|seven|eight|
|-- |-- | -- | -- | -- |--|--|--|--|--|--|--|--|--|--|--|--|--|--|--|--|
|souliss:t11|x|x|x|x||||
|souliss:t12|x| |x|x|x||||
|souliss:t13|||x|x||x|x||
|souliss:t14|||x|x||||x|
|souliss:t16|x|x|x|x|||||x|x|x|x||
|souliss:t18|x||x|x|||||||||
|souliss:t19|x|x|x|x||||||x|x|
|souliss:t1A|||x|x|||||||||x|x|x|x|x|x|x|x|

|Thing type | lastStatusStored | healty|rollershutter|rollershutter_state|mode|status|setpoint|setAsMeasured|measured|statusAlarm|onOffAlarm|rearmAlarm|
|-- |-- | -- | -- | -- |--|--|--|--|--|--|--|--|
|souliss:t21|x|x||x|
|souliss:t22|x|x|x|x|
|souliss:t31|x|x|||x|x|x|x|x|
|souliss:t41|x|x||||||||x|x|x|

|Thing type | lastStatusStored | healty|value|
|-- |-- | -- | --|
|souliss:t51|x|x|x|
|souliss:t52|x|x|x|
|souliss:t53|x|x|x|
|souliss:t54|x|x|x|
|souliss:t55|x|x|x|
|souliss:t56|x|x|x|
|souliss:t57|x|x|x|
|souliss:t58|x|x|x|

|Thing type | lastStatusStored | healty|value|
|-- |-- | -- | --|
|souliss:t61|x|x|x|
|souliss:t62|x|x|x|
|souliss:t63|x|x|x|
|souliss:t64|x|x|x|
|souliss:t65|x|x|x|
|souliss:t66|x|x|x|
|souliss:t67|x|x|x|
|souliss:t68|x|x|x|
|souliss:topic|x||x|

## Manual Things Configuration

If after discovery your thing is not listed you can add it manually.
You have to choice it from disponible items. Firts gateway, after items!
To configure Gateway you can leave default value on Thing ID and write your value on "IP or Host Name" and "Gateway port".

To configure a typical (items) you have to choice your "Name" and "Location", you have to choice your "Gateway" and insert correct "Thing ID".

Thing ID is [node]-[slot]
For example, if you have two nodes and you want configure a typical on second node at slot seven, you must write 
Thing ID: 
```
2-7
```


## Basic UI and Classic UI
Examples to configure items in Basic UI and Classic UI
Thing <binding_id>:<type_id>:<thing_id> "Label" @ "Location" [ <parameters> ]
    
The general syntax for .things files is defined as follows (parts in <..> are required):
```
Bridge <binding_id>:<type_id>:<bridge_id> "<Souliss Gateway Name>" [ <parameters> ]
{  
Thing <type_id> <thing_id>  [ <parameters> ]
}

```


souliss.things:
```
Bridge souliss:gateway:105 "Souliss Gateway - 105" [GATEWAY_IP_ADDRESS="192.168.1.105", GATEWAY_PORT_NUMBER=230, PREFERRED_LOCAL_PORT_NUMBER=0, PING_INTERVAL=30, SUBSCRIBTION_INTERVAL=2, HEALTHY_INTERVAL=33, USER_INDEX=71, NODE_INDEX=20]
{  
Thing t11 12-0 [sleep=20]
Thing t31 6-0
}
```
You have to write your Gateway IP Number and leave all other to default values


default.items:

```
Group    Home                     "Tonino"        <house>
Group    FamilyRoom               "Soggiorno"     <parents_2_4>   (Home)
Group    Outside         "Esterno"   <garden>   (Home)
Group    HomePower
Group Diagnostic

Switch   tettoia  "Tettoia"  <light>    (Outside)   ["Lighting"]   {channel="souliss:t11:1-0:onoff"}
Switch   portoncino         "Portoncino"          <light>         (FamilyRoom)         ["Lighting"]   {autoupdate="false",channel="souliss:t14:1-6:pulse"}
Switch   cancello         "Cancello"          <light>         (FamilyRoom)         ["Lighting"]   {autoupdate="false",channel="souliss:t14:1-7:pulse"}
Number   Power      "Power [%.1f W]"       <energy>      (FamilyRoom, HomePower)                     {channel="souliss:t57:1-4:value"}
Number   FamilyRoom_Temperature   "Temperatura [%.1f °C]"   <temperature>  (FamilyRoom)                  {channel="souliss:t31:6-0:measured"}
Number   FamilyRoom_Humidity      "Umidità [%.1f %%]"       <humidity>      (FamilyRoom)                     {channel="souliss:t53:6-7:value"}
String	UpdateNode1	"Power update [%1$td.%1$tm.%1$tY %1$tk:%1$tM:%1$tS]"	<keyring> (FamilyRoom, Diagnostic)  {channel="souliss:t57:1-4:lastStatusStored"}
```

default.sitemap:

```
sitemap default label="Tonino" {
    Frame {
        Text label="Rientro casa" icon="light" {
           Switch item=portoncino mappings=[ON="Open"]
           Switch item=cancello mappings=[ON="Open"]
        }
    }     
     Frame {
        Group item=Outside
    }
        
Frame {
        Text label="Temperature and Humidity" icon="temperature" {
            Default item=FamilyRoom_Temperature label="Temp."
            Default item=FamilyRoom_Humidity label="Hum."
            Default item=AggiornamentoNodo6 icon="icon16x16"
        }
        Text item=FamilyRoom_Temperature label="Temp. [%.1f °C]"
        Text item=FamilyRoom_Humidity label="Hum. [%.1f %%]"
        
        Text item=Power label="Hum. [%.1f W]"
        Text item=UpdateNode1
    }
   
}
    
```



## Community

Souliss is a small community and actualy it don't have sufficient human resource to be more active on Openhab official community

These are some very popular forum:

English Group, [here](https://groups.google.com/forum/#!forum/souliss)

Italian Group, [here](https://groups.google.com/forum/#!forum/souliss-it)

Spanish Group, [here] (https://groups.google.com/forum/#!forum/souliss-es)

## Contribution
Officiale repository for contribution in souliss github area: [here](https://github.com/souliss)



## Download 

To download latest compiled binding go to releases tab: [here](https://github.com/fazioa/soulissBindingOH2/releases)
