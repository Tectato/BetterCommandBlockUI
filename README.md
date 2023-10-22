# Better Command Block UI
A clientside minecraft mod for providing a more usable Command Block UI

# Description
*Tired of holding down your arrow keys to get to the middle of a long command, only to overshoot and spend another half an eternity backtracking? Wishing you could just see the whole thing in a more structured format without needing to copy it into external software and back again?*
This mod intends to address these issues by overhauling the Command Block UI, mainly by extending the single-line command text field into a multi-line text box, complete with vertical and horizontal scrolling as well as automatic parentheses-based indentation! Additionally the large buttons used to cycle the command block type and toggle things like powering mode have been reduced to simple icons with tooltips, and an extra button has been added to switch between displaying the command and the previous output.
![image](https://github.com/Tectato/BetterCommandBlockUI/assets/89499782/1675e399-b63e-4913-a111-c104ac7b0001)
The mod is entirely clientside, it takes in the command as received from the server and just displays it across multiple lines and with indentation, but doesn't actually add any line breaks to it.

It's also fully compatible with [NBT Autocomplete](https://modrinth.com/mod/nbt-autocomplete), a fantastic mod that I cannot recommend enough for anyone working with commands on the regular!

Another feature: pressing a configurable keybind ( ; by default) will select one corner of an area based on your current position. Once you've selected two corners, an equivalent area selector (x=..,y=..,z=..,dz=..,dy=..,dz=..) will be put in your clipboard for easy pasting into commands!
