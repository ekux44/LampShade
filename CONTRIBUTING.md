## Getting Started
* Make sure you have a [GitHub account](https://github.com/signup/free).
* Open a GitHub issue for your bug-fix/feature, assuming one does not already exist.
  * Clearly describe the issue including steps to reproduce when it is a bug.
* Fork the repository on GitHub.

## Code Setup
1. Download [Android Studio](http://developer.android.com/sdk/index.html).
2. Download this repository from Github.
3. Open Android Studio -> Import Project -> select the folder containing this repository.
4. You may have to download additional versions of the Android SDK through Android Studio.

## Code Style
This project uses Google's Java code style formatter rules instead of the Android code style used by default in Android Studio. intelij-java-google-style.xml contains these rules. This can be imported at Android Studio -> Preferences -> Code Style -> Manage

## Making Changes

* Create a topic branch from where you want to base your work.
  * This is usually the master branch.
  * To quickly create a topic branch based on master; `git checkout -b
    fix/master/my_contribution master`. Please avoid working directly on the
    `master` branch.
* Make commits of logical units.
* Check for unnecessary whitespace with `git diff --check` before committing.
* (Recommended) Run the Android Studio code formatter on files you've modified.
* Run the androidTest unit tests if you have made changes to any classes with test coverage.

## Making Trivial Changes

For changes of a trivial nature to comments and documentation, it is not necessary to open a tracking github issue.


## Submitting Changes

* Push your changes to a topic branch in your fork of the repository.
* Submit a pull request to the (official repository](https://github.com/ekux44/LampShade)
