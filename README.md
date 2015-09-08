# LenkaSpaceAntConstruction

A little modification I did for an assignment of an ant construction simulation > [Original code](http://lenkaspace.net/#lab/swarmSystems/controllingAntConstruction).

I was still getting used to Java at the time, so this was a neat challenge. I learnt that Java is a bit unwieldy for doing matrix calculations, as I used the JBLAS package which has native BLAS libraries. There is probably a much more idiomatic way of doing it. I also discovered the critical importance of source version control. The model broke at one point and exhibited very weird behaviour, and I spent a few frustrating days hunting down the source of the bug, before discovering that it was a constant I'd randomly changed at some point to see the effect. It was a pretty valuable experience to work with someone else's code too.
