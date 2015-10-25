# Viskell

[![Build Status](https://travis-ci.org/wandernauta/viskell.svg?branch=master)](https://travis-ci.org/wandernauta/viskell)

Viskell is an experimental visual programming environment for a typed (Haskell-like) functional programming language.
This project is exploring the possibilities and challenges of interactive visual programming in combination with the strengths and weaknesses of functional languages.

![Screenshot](screenshot.png)

While many visual programming languages/environments exist they are often restricted to a specific application domain or a special target audience.
The potential of an advanced type systems and higher level abstractions in visual programming has barely been explored yet.

### Goals and focus points

  * Creating readable and compact visualisations for functional language constructs
  * Immediate feedback on every program modification, avoiding the slow edit-compile-debug cycle
  * Exploring alternative inputs methods, i.e. multi-touch devices
  * Type-guided development: program fragments show their types, and type error are visualised.
  * Programming with less local variables ('wirefull' approach as alternative to 'pointfree' style).
  * Raising the level of abstraction (good support for higher order functions and other common Haskell abstractions).
  * Addressing the scalability issues common to creating large visual programs.
  * Supporting collaborative programming on eg. a large multi-touch screen.
  * Refactoring as a first class programming action.

### Status

Viskell is not yet usable for any practical purpose, but suggestions or feedback from overly interested souls is very welcome.
Many essential basic features are still missing and te implementation is in the middle of big rewrite.
The plan is to have demo-ready version with basic features by the end of the year.

### Building Viskell

To build an executable `.jar` file that includes dependencies, check out this repository, then run

    mvn package

Java 8 is required. Importing as a Maven project into any Java IDE should also work.
