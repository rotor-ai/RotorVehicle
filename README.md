# Rotor Vehicle

The RotorVehicle library provides an architecture for controlling an Android-based autonomous vehicle. The Rotor can either be controlled manually from a client, or in autonomous mode (coming soon). 

<img src="https://github.com/rotor-ai/RotorVehicle/blob/develop/screenshot.png" width="350px"/>

## Getting Started

### Prerequisites

The RotorVehicle library has been tested with the following configuration:
- Hardware: Raspberry Pi 3
- OS: Android Things 1.0.6

Additional Hardware:
- Android or iOS device running the Rotor `mobileclient` library - https://github.com/rotor-ai/mobileclient
- Arduino running the `RotorArduino` motor controller library - https://github.com/rotor-ai/RotorArduino
- RC car with PWM speed control and servo for steering - a good starting point is the Exceed Racing Desert Short Course Truck - [Amazon](https://www.amazon.com/Exceed-Racing-Desert-Course-2-4ghz/dp/9269802108/ref=sr_1_fkmrnull_1?keywords=Exceed+Racing+Desert+Short+Course+Truck&qid=1554669149&s=toys-and-games&sr=1-1-fkmrnull)
- (Optional) A Raspberry Pi LCD screen can be used for logging and debugging - [Amazon](https://www.amazon.com/gp/product/B0153R2A9I/ref=ppx_od_dt_b_asin_title_s01?ie=UTF8&psc=1)

### Installing

Download the RotorVehicle library to your computer:

```
git clone https://github.com/rotor-ai/RotorVehicle
```

Upload the code to the pi, and you're off!

## Authors

* **Robert Humphrey** - *RotorVehicle & RotorArduino*
* **Stuart Bowman** - *RotorVehicle & MobileClient*

## License

This project is licensed under the Apache 2.0 License - see the [LICENSE.md](LICENSE.md) file for details
