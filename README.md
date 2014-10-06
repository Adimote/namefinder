namefinder
==========
A robust web scraper to get the real name from the username in the Southampton ECS people database ([this](http://www.ecs.soton.ac.uk/people/)).
It works by initially loading the page for 'dem', it then finds the string "Dr David Millard" in the html, and .
This can then be used for all other users, and is much more robust than the default hard-coded scraper.

It also searches the html whilst ignoring lines, using the java 'scanner' object to prevent the script breaking if a web-developer minifies the HTML or adds newlines.

