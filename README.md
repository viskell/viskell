# Viskell

Viskell is an experimental visual programming environment for a typed (Haskell like) functional programming language.
This project is exploring the possibilities and challenges of interactive visual programming in combination with the strenghts and weaknesses of functional languages.
While many visual programming languages/environments exist they are often restricted to a specific application domain or a special target audience.
The potential of an advanced type systems and higher level abstractions in visual programming has barely been explored yet.

### Goals and focus points
  * Creating readable and compact visualisation for functional language constructs.
  * Direct feedback on every program modification (avoiding the slow edit-compile-debug cycle).
  * Exploring alternative inputs methods (for now multi-touch devices).
  * Type guided development (program fragments show their types, and type error are visualised).
  * Programming with less local variables ('wirefull' approach as alternative to 'pointfree' style).
  * Raising the level of abstraction (good support for higher order functions and other common Haskell abstractions).
  * Addressing the scalability issues common to creating large visual programs.
  * Supporting collaborative programming on eg. a large multi-touch screen.
  * Refactoring as a first class programming action.

### Status
Not useable for any practical purpose, but suggestions or feedback from overly interested souls is welcome.
  * Many essential basic features are still missing.
  * The implementation is in the middle of big rewrite.
  * Plan is to have demoable version with basic features by the end of the year.

### Installation
Import as a Maven project into a Java IDE, also requires JavaFX support.
