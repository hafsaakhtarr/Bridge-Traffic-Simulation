# Bridge Segment Traffic Simulation

A DEVS-based discrete event simulation modeling a three-bridge traffic system with bidirectional flow and traffic light control.

## Project Overview

This project implements a bridge traffic simulation system. 1. The system consists of three bridge segments connecting east and west, with traffic lights controlling bidirectional vehicle flow.

### System Components

- **BridgeSegment**: Atomic DEVS model representing a single bridge with traffic lights
- **BridgeSystem**: Coupled model connecting three bridge segments
- **CarGenerator**: Generates vehicles at configurable intervals
- **Transducer**: Collects and records exit events for analysis

## Architecture

```
West                                                East
  |                                                  |
WestGen → [Bridge3] → [Bridge2] → [Bridge1] → EastGen
  |          ↓           ↓           ↓           |
  |       (outputs)   (outputs)   (outputs)      |
  |          ↓           ↓           ↓           |
  └─────── Transducer (collects all exit events) ←┘
```

### Traffic Flow
- **WestGen**: Generates eastbound traffic (west → east)
- **EastGen**: Generates westbound traffic (east → west)
- Vehicles flow through all three bridges sequentially
- Each bridge takes 10 seconds to cross

## Key Features

### BridgeSegment Model

**States:**
- `idle`: Waiting for vehicles or light switch
- `crossing`: Vehicle currently crossing (10 seconds)

**Behavior:**
- Traffic lights switch at fixed intervals (configurable per bridge)
- Vehicles can only cross when:
  1. Their direction has green light
  2. Sufficient time remains before next light switch (≥10 seconds)
- Supports back-to-back crossings during green phases
- Independent queues for eastbound and westbound traffic

**Key Algorithm:**
```java
timeUntilSwitch = lightSwitchTime - currentSimulationTime
if (timeUntilSwitch >= 10 seconds && correct_direction && vehicle_available) {
    start_crossing();
}
```

## Configuration

### Bridge Settings

Each bridge can be configured with:
- **Initial State**: `WEST_TO_EAST` or `EAST_TO_WEST`
- **Traffic Light Duration**: Time in seconds (e.g., 30, 100)

Example configuration:
```java
AbstractBridgeSystem.BridgeSystemSetting.Bridge1InitialState =
    AbstractBridgeSystem.BridgeState.WEST_TO_EAST;
AbstractBridgeSystem.BridgeSystemSetting.Bridge1TrafficLightDurationTime = 100;
```

### Test Cases

**Case 1: Long green phases (100s)**
```java
All bridges: WEST_TO_EAST, 100s green time
Result: ~34-36 events, efficient throughput
```

**Case 2: Short green phases (5s)**
```java
All bridges: WEST_TO_EAST, 5s green time
Result: 0 events (insufficient time to cross)
```

**Case 3: Mixed configuration**
```java
Bridge3: EAST_TO_WEST, 100s
Bridge2: EAST_TO_WEST, 5s
Bridge1: EAST_TO_WEST, 100s
Result: Testing asymmetric flow
```

**Case 4: Medium green phases (30s)**
```java
All bridges: EAST_TO_WEST, 30s green time
Result: ~48 events, high frequency switching
```

## Implementation Details

### Critical Design Decisions

1. **Absolute Time Tracking**: Uses `lightSwitchTime` as absolute simulation time for precise light switching
2. **Time Calculation**: `timeUntilSwitch = lightSwitchTime - getSimulationTime()`
3. **Safety Check**: Only start crossing if `timeUntilSwitch >= crossTime`
4. **Independent Timing**: Traffic light schedule is independent of crossing events

### State Transitions

```
IDLE State:
  - External: Vehicle arrives → try crossing if sufficient time
  - Internal: Light switch time reached → switch direction

CROSSING State:
  - Internal: Crossing completes → try next crossing or switch light
```

## Running the Simulation

### Prerequisites
- DEVSJAVA framework
- Java 8 or higher

## Project Structure

```
HAkhtar/
├── BridgeSegment.java    
└── BridgeSystem.java       

BridgeSegment/              
├── AbstractBridgeSystem.java
├── CarGenerator.java
├── Transducer.java
└── Test.java
```

## Performance Analysis

- **Green Time = 100s**: Optimal throughput, ~34 events
- **Green Time = 30s**: Higher frequency switching, ~48 events
- **Green Time = 5s**: No throughput (insufficient crossing time)
- **Crossing Time**: Fixed at 10 seconds per vehicle

## Known Limitations

1. Road segments between bridges have zero travel time (instant transfer)
2. Infinite queue capacity at each bridge
3. Random car generation may produce slightly different event counts
4. Single lane (no passing or parallel crossing)

