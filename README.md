# Viskell

[![Build Status](https://travis-ci.org/wandernauta/viskell.svg?branch=master)](https://travis-ci.org/wandernauta/viskell)

Viskell is an experimental visual programming environment for a typed (Haskell-like) functional programming language.
This project is exploring the possibilities and challenges of interactive visual programming in combination with the strengths and weaknesses of functional languages.

![Screenshot](screenshot.png)

While many visual programming languages/environments exist, they are often restricted to some application domain or a specific target audience.
The potential of advanced type systems and higher level abstractions in visual programming has barely been explored yet.

### Background information
[Short project overview presentation](viskell-nlfpday.pdf) as given on the Dutch Functional Programming Day of Jan 8, 2016.

### Goals and focus points

  * Creating readable and compact visualisations for functional language constructs.
  * Immediate feedback on every program modification, avoiding the slow edit-compile-debug cycle.
  * Experimenting with a multi-touch focused user interface, supporting multiple independently acting hands.
  * Type-guided development: program fragments show their types, and type error are locally visualised.
  * Raising the level of abstraction (good support for higher order functions and other common Haskell abstractions).
  * Addressing the scalability issues common to creating large visual programs.

### Status

Viskell is not yet usable for anything practical, however suggestions from curious souls are very welcome.
While being nowhere near a complete programming language, most basic features have an initial implementation.
Every aspect of the design and implementation is still work in progress, but ready for demonstration purposes and giving an impression of its potential.

### Building Viskell

To build an executable `.jar` file that includes dependencies, check out this repository, then run

    mvn package

Java 8 is required. Importing as a Maven project into any Java IDE should also work.
