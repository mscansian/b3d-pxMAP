b3d-pxMAP
=====
Load .map (Valve Hammer Editor) files into Blitz3d primitives.


Features
---------
* Full texture support
* Works with carved meshes
* Support for Valve Hammer Editor entities

License
-----------
[GNU LGPLv3](https://www.gnu.org/licenses/lgpl.html)

Notes
---------
This project was never intended for public release, so don't expect a good and understandable code. 
Even though I used this lib extensively, you still may find some bugs. If you do, please fill out an issue report or make the changes youself and send me a pull request.

There are some simplifications in the mesh calculations. Some errors might appear when loading very complex (i.e with lots of faces) meshes.

This lib is perfect for small maps. Beware that occlusion culling is NOT implemented, so some lag can happen with large maps.
