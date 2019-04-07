# Rotor Vehicle

The RotorVehicle library provides an architecture for controlling an Android-based autonomous vehicle. The Rotor can either be controlled manually from a client, or in autonomous mode (coming soon). 

## Getting Started

### Prerequisites

The RotorVehicle library has been tested with the following configuration:
- Hardware: Raspberry Pi 3
- OS: Android Things 1.0.6

Additional Hardware:
- Android or iOS device running the Rotor `mobileclient` library - link: https://github.com/rotor-ai/mobileclient
- Arduino running the `RotorArduino` motor controller library - link: https://github.com/rotor-ai/RotorArduino
- RC car with PWM speed control and servo for steering - a good starting point is the Exceed Short Course Desert Truck - link: [Amazon](https://www.amazon.com/Exceed-Racing-Desert-Course-2-4ghz/dp/9269802108/ref=sr_1_fkmrnull_1?keywords=Exceed+Racing+Desert+Short+Course+Truck&qid=1554669149&s=toys-and-games&sr=1-1-fkmrnull))
- (Optional) A Raspberry Pi LCD screen can be used for logging and debugging - link: [Amazon](https://www.amazon.com/gp/product/B0153R2A9I/ref=ppx_od_dt_b_asin_title_s01?ie=UTF8&psc=1)

### Installing

Download the RotorVehicle library to your computer:

```
git clone <RotorVehicle-repo>
```

Upload the code to the pi, and you're off!


## Contributing

Please read [CONTRIBUTING.md](https://gist.github.com/PurpleBooth/b24679402957c63ec426) for details on our code of conduct, and the process for submitting pull requests to us.

## Authors

* **Robert Humphrey** - *RotorVehicle & RotorArduino*
* **Stuart Bowman** - *RotorVehicle & MobileClient*

See also the list of [contributors](https://github.com/your/project/contributors) who participated in this project.

## License

This project is licensed under the Apache 2.0 License - see the [LICENSE.md](LICENSE.md) file for details
