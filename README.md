# <img src="https://github.com/mkulesh/molecularDynamics/blob/master/images/ic_launcher.png" align="center" height="48" width="48"> "Molecular Dynamics - Classical molecular dynamics method combined with HD live wallpaper"

MMD is a classical molecular dynamics code implemented as an App for Android. MMD is acronym for Method of Molecular Dynamics, which is a computer simulation of particles (atoms, molecules) motion. This simulation is done with respect to the given initial and boundary conditions and a potential that describes the particle interaction.

The trajectories of the particles are determined numerically. The well known Velocity Verlet integration of Newton's equation of motions is used for a system of interacting particles. Forces between particles are defined analytically using a "pair potential function" that depends on the distance between two particles.

In physics, MMD is used to examine the dynamics of atomic-level phenomena that cannot be observed directly. Within the mobile devices context, it can be used as a small mobile laboratory to understand the basics of the particle physics. Furthermore it can be used to produce nice dynamical pictures that are suitable as a live wallpaper.
![main view](https://github.com/mkulesh/molecularDynamics/blob/master/images/main_view_hor.png)

## Application features:

- The application can be set as a live wallpaper that visualizes the current experimental set up.
- In the live wallpaper mode, the active “static” wallpaper can be used as a background. You can mix your favorite background picture with particles “flying” in front of it.
- The changes made in main application will be applied to live wallpaper daemon to make the wallpaper tuning easier.
- Generation of an initial structure of the particles use diagonal or square grids with given dimension
- Possibility to select a boundary condition (full energy is constant, kinetic energy is constant, or no constraints)
- If kinetic energy is constrained, it is possible to apply “thermal change” that simulates heating/cooling of the system
- Possibility to change the gravity value. The gravity vector can be also changed by rotating of the device.
- Possibility to select and visualize selected potential. Currently, three common potentials are available: Lennard-Jones, Morse and Born-Mayer.
- The particle skin can be selected from build-in clip art.
- Multi-touch interface is used to zoom and drag the experimental area.

Note: since the application implements a numerical integration method, it generates a significant CPU load. Therefore, the live wallpaper mode is not suitable for low-power devices.


<a href='https://play.google.com/store/apps/details?id=com.mkulesh.mmd&hl=en'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png' width=323 height=125/></a>

## Languages
* English
* Russian

## License
This software is published under the *GNU General Public License, Version 3*

Copyright (C) 2014-2017 Mikhail Kulesh

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along with this program.

If not, see [www.gnu.org/licenses](http://www.gnu.org/licenses).

## Dependencies

This App depends or includes the following third-party libraries or code fragments:
* [The Android Support v7 Library](https://developer.android.com/topic/libraries/support-library/packages.html)
* [AndroidSVG Library](https://github.com/BigBadaboom/androidsvg)

