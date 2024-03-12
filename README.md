# TodoTree (Android App)

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" alt="Get it on F-Droid" height="80">](https://f-droid.org/packages/nl.tsmeets.todotree)
[<img src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png" alt="Get it on Google Play" height="80">](https://play.google.com/store/apps/details?id=nl.tsmeets.todotree)

Keep your mind clear by quickly creating a nested tree of notes and tasks.

Some example usages of this app are:

* Creating quick notes on a given subject
* Exploring complicated ideas for potential projects.
* Maintain a list of tasks that have to be done
* Maintain a list of long term tasks with notes.
* Keep a groceries list

## Design

The whole interface is designed to allow for quick navigation of a deep and complicated tree of tasks and notes.
Adding tasks and notes is also very quick and easy.

![](doc/info.png)

In the above screenshot you can see that the current focused node is "finish TodoTree v1.4". This node contains a number of children:

* "update demo screenshot"
* "add settings"
* "finish translations"
* "improve documentation"
* "upload"

And a successive list of parents:

* "Wednesday"
* "Week"
* "TodoTree Demo"

In this case you can see that the parent node of "finish TodoTree v1.4" is "Wednesday". But here the parent of "Wednesday" is also visible, this is "Week".
And the parent of "Week" is also visible which is "TodoTree Demo". The top most node is "root". Each of these parent nodes can be visited by tapping on their name.

This list serves two purposes. The first is that it allows you to immediately see where exactly the current node is located.
The second purpose is that it allows you to immediately travel back to any level in the tree, making the navigation very quick.

Nothing will stand in your way from quickly navigating the tree and adding tasks and notes.

## Contributing
If you have an idea for a new feature or have found a bug, please [open an issue](https://github.com/TomSmeets/TodoTree/issues) on GitHub.

## Donation
If you'd like to support my work the please consider donating either on
[GitHub](https://github.com/sponsors/TomSmeets),
[Liberapay](https://liberapay.com/tsmeets) or
[PayPal](https://www.paypal.com/donate/?hosted_button_id=9FYM8Q5LXEFLY)

## License
TodoTree is licensed under the GPLv3 license. Please see the [license](LICENSE.txt) file for more details.
