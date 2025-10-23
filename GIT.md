Brief guide on git and its usage

# Setting up the repository on your local machine

## SSH (recommended)

In order to use SSH, you must [set up a SSH key](https://docs.github.com/en/authentication/connecting-to-github-with-ssh/generating-a-new-ssh-key-and-adding-it-to-the-ssh-agent) and [add it to GitHub](https://docs.github.com/en/authentication/connecting-to-github-with-ssh/adding-a-new-ssh-key-to-your-github-account).
```sh
git clone git@github.com:CMPUT301F25logout/logout-evently.git
```

## HTTPS

This may prompt you for credentials each time.

```sh
git clone https://github.com/CMPUT301F25logout/logout-evently.git
```

Once you have the repo set up locally, make sure to `cd` into `logout-evently` for any further commands.

# Check your bearings

Run `git status` often to know which branch you're on and the state of your working repository.

Run `git checkout chase/event-screen` to switch to the **_existing_** branch `chase/event-screen`. This branch must either exist upstream, and/or locally.

Sometimes, you'll see a branch exists on github but when you try to checkout to it locally - it tells you that the branch doesn't exist. Run `git fetch` when that happens. Your local git doesn't know about the upstream branch yet.

# Updating local repository

If there have been new commits on the upstream branch (e.g `main`) since the last time you cloned/pulled, you can simply do `git pull` while you're on said branch (e.g `main`) and it'll update the branch. If you're using an IDE, there should be a simple button for this as well.

However, if you have local changes that you have not committed yet, pulling can be problematic. This is unlikely to happen on `main` because you should NEVER modify it locally.

In general, the best way to avoid this is to ensure multiple people are **not working on _the same branch_**. In the rare case where this is unavoidable, make sure to communicate between each other and instruct the other person to pull your changes before they make their changes.

In the even unlikelier case, where you have made local changes and someone else has made upstream changes - you must perform a merge. Follow these steps:

- Run `git reset --soft HEAD~k`, but replace `k` with a number (like `1`, `2`, `3`). This is the number of commits you'd like to "soft revert". Basically, you're uncommitting your local changes upto the point where both your local history and upstream history line up.
- `git stash` to save your changes for later.
- `git pull`
- `git stash pop` - this will most likely cause a merge conflict. You need to fix it to continue.

  Read more about resolving merge conflicts in the [section below](#merge-conflicts)

# Making a branch for your changes

```sh
git checkout -b chase/event-screen
```
If I run the above command while on branch `main`, git will make a new branch named `chase/event-screen`, _as a copy of_ `main`, **and** _switch to it_.

Right now, this branch **ONLY** exists _locally_. In order to create it upstream and associate your local with it, you'll have to make a commit and push it using:

```
git push -u origin chase/event-screen
```

Everything after and including the `-u` is only required the **very first time** you push a brand new branch upstream. Next time, you can just do `git push`.

IDEs usually have a push button that does all of this for you.

## Choosing the right branch to branch off of

Notice how `git checkout -b` creates a new branch as a copy of the branch you're currently on. It's important to decide which branch you want to copy/template. In general, you want the template branch to be the one that includes any dependencies for your work.

Example: I want to work on the entrant browse event screen. My goal is to set up the basic listview and stuff. I know I'll need the `Event` class so I can list a bunch of them. I also know that `Event` is one of those common classes that will probably be used everywhere. So I should first see if this class already exists in the codebase.

> Assume `Event` class is my only dependency for this work. In case of multiple dependencies, I'll have to find a branch that includes all of them, or make one.

Does `Event` exist on main?
  Yes -> Branch off of main
  No -> Does it exist on a different branch?
    Yes -> Branch off of said branch (execute `git checkout -b` while on that branch)
    No -> Make one to implement `Event` or ask group

# Staging and unstaging

`git add` with a target path will add that path to "staging". Staging includes changes that will be added to the commit when you perform `git commit`.

Any changes that are not yet `git add`ed will be in your unstaged section. These will not be added to the commit when you do `git commit`.

If you have staged a change, but you no longer want to keep it staged (don't want it in the commit), you can do `git restore --staged` with the path name.

Example: I'm changing `./src/Entry.java` and I want it staged. I can do `git add ./src/Entry.java`. Later, I decided not to keep it staged. I can do `git restore --staged ./src/Entry.java` and it will go back to the unstaged section (the changes will still be in the file).

Advice: Use your IDE to manage this if you're not confident with git. IDEs usually have simple functionality to add and remove files/changes to and from staging.

# Committing changes

It's important to decide which files/changes you want to commit. Most beginners just do `git add .` and then `git commit`. This is not always a good idea.

It's likely that `git add .` will be _fine_ for the most part in this project. However, at the very least, check the changes you're about to commit before you `git commit` using `git status`.

It's important to note that if you do `git add` on fileA, then make some changes to fileA, and `git commit` without `git add`-ing fileA again - you'll be only be committing the old fileA (before the recent changes). Be aware and `git add` when you want new changes to be included.

Note: When you run `gradlew spotlessApply` to format your files, there WILL be new changes (if required). You need to `git add` these to the commit. It's silly to do `gradlew spotlessApply`, not `git add` the changes, `git commit` and expect CI to pass. Your formatting changes never went into the commit!

Advice: Use your IDEs version control system to choose and add changes/files and commit from there. It's usually easier for beginners.

# Changing branches with pending work

Changing branches sounds simple. You just do `git checkout branchName` and assuming `branchName` already exists, you'll switch to it. However, things become complicated when you have pending changes in your current branch that you haven't yet committed and you're trying to change branches (maybe to update it).

In this case, you can either add and commit all your changes (if you think they're ready), or you can stash them (recommended if your changes aren't ready for a commit).

Easiest way to stash is `git stash`. This will store your unstaged changes. If you also have staged changes, you can either unstage them before running `git stash` or do `git stash --staged` to store specifically the stashed changes.

Now you can change your branch, do other stuff and when you come back later to this branch to continue your work. You can `git stash pop`. This will remove* and apply the last group of changes you stashed. The git stash is essentially a stack. New stashes are added to the top. And popping applies the stash at the top.

*: Popping won't always remove a stash. In case of merge conflicts, it might keep it. When you perform `git stash pop` and see the command line output - at the very bottom, you'll see if git decided to still keep that stash for later.

You can do `git stash apply` instead to apply the latest stash without removing it.

For git beginners, it's highly recommended to actually name your stashes (especially if you struggle remembering what you stashed and when). Use `git stash push -m "stash name"` or `git stash push --staged -m "stash name"` to save stashes with names. This will allow you to see the names in the output of `git stash list`:

```
stash@{0}: On branch1: stash name
stash@{1}: On branch1: another stash
```

Notice the numbers, you can use the number to apply/pop the specific stash. `git stash apply 1` or `git stash pop 1` will apply the stash with the name "another stash".

# Updating your branch

Throughout the project, you'll be doing a LOT of merges. This is purely because there's so many people working on such a small project. While you're working on your PR/branch, `main` might have had a bunch of updates (other PRs are merged in). This means that you'll have to merge the updated main into your branch in order to continue with your PR.

If you see upstream `main` has been updated. You should switch to `main` (see [changing branches](#changing-branches-with-pending-work)) and perform a `git pull`. Afterwards, you can switch back to your branch and do `git merge main`. It's very likely there will be merge conflicts - you need to resolve them properly.

# Merge Conflicts

This will likely be the hardest thing you'll have to do with git in this project. Merge conflicts are inevitable in a group of this size.

> If you have any interest in working in the industry, you might want to know that merge conflicts are an everyday thing. Don't ignore them.

You _must_ to use a text editor to work with merge conflicts. Different text editors have different usefulness. In the most basic case, this is what conflicts look like in raw text:

```java
<<<<<<< HEAD
  void foo() {
    String[] events;
    events.init(x -> x.lower());
=======
  void foo() {
    Event[] events;
>>>>>>> other_branch
```

The stuff between `<<<<<<< HEAD` and `=======` are "current branch changes".

Stuff between `=======` and `>>>>>>> other_branch` are conflicting changes from `other_branch`. i.e, result of `git merge other_branch`.

In the above example, `other_branch` has updated the type of `events` to `Event`, rather than using raw strings. This is clearly a good change and we want to keep it!
However, on my own branch, I have an initialization of the `events` which works with strings! So I'll have to update it to work with event.

A proper resolution should get rid of all the `<<<<` and `====` stuff and just write the final code:

```java
void foo() {
    Event[] events;
    events.init(x -> new Event(x.toString().lower()));
```

In IDEs, you'll usually be shown three windows. One will show "current changes", another will show "incoming changes" (other branch), and a third window where you'll need to put in what the final resolved changes should be. You may copy the relevant parts of the current and incoming changes and paste it into the third window.

Many IDEs can smart merge most things. There are ways to "accept both" changes in IDEs which _often_ resolves correctly, but not always!

If you're lucky, you might just be able to accept one side's change unequivocally. This won't happen often though.

# Conventions

1. Do not duplicate work. If you're planning to work on some component that might be used in another groupmate's work, communicate with them and make a common branch that both of you can use.
2. Work on individual branches, ideally solo. Name your branches with this template: `<your name>/<change-title>`. Try to be specific enough with the `<change-title>` without being too verbose. For example, if I wanted to work on the initial foundation for lottery selection, I might name my branch `chase/lottery-selection-init` or `chase/lottery-init`.
3. Learn how to merge well. Do NOT ignore conflict resolution. This is a project with lots of concurrent work and merge conflicts are inevitable. Pay attention during merges and _avoid losing significant changes_ during conflict resolution.

  This is critical. You don't want to accidentally drop bug fix A while merging the `bug-fix-A` branch with your branch. **Respect** changes being merged in - don't just drop them unless you have a good reason to.
4. In general, it's important to make granular commits with descriptive commit messages. However, the repo is set up in such a way that individual commits are not preserved. They'll instead be all squashed into one when a PR is merged.

  All that is to say: Use descriptive PR titles. Try to keep your PRs small-ish. Don't make a PR that says "Develop End User Activity" (that'd be a huge PR). Make a PR for "Foundation for main event brose screen for entrants".
5. Update your branch often. `main` will likely be updated almost everyday. Make sure you keep your work up to date with `main`. Incremental merges are usually easier than falling behind and merging at the very end.
6. Commit often. You don't want to lose changes. Especially as a git beginner, you may screw things up and you'll regret losing all your hard work.

  Even I sometimes lose changes but I usually commit often and occassionally employ git black magic to recover from my mistakes. Don't rely on that though.