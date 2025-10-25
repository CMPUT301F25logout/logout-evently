1. Be specific in your type signatures. Don't write code as if it's a dynamically typed language. Leverage generics.
2. NEVER treat `null` as a potential cohabitant of the objects you generate. We can't guarantee the APIs we use won't hand us `null`s, but we should try our best to guarantee that we don't hand each other `null`s. Assume that ALL of our own objects (stuff we create) are `NonNullable`. Don't return nulls, don't ask for nullable inputs.

    Mark nulls explicitly. Use the `Option` type. If you want an optional string, use `Optional<String>`, not just `@Nullable String`. These annotations are not thoroughly checked (because it's impossible*) by any compiler. It's only best effort.

    Null is termed "the billion dollar mistake" by its creator. DO NOT USE IT.

    *: The data flow analysis for tracking nullable types is undecidable.
3. As discussed in the modern java practices slides, follow [data oriented programming](https://inside.java/2024/05/23/dop-v1-1-introduction/) practices, summarized below:
    - Think with functions, not classes/objects. Avoid wasting time on thinking about big actor hiearchies. Just think about the technical functionality.
    - Separate data from code. Avoid large classes/objects with lots of mutable state. Ideally, avoid any and all immutable state whenever possible.

      Use records!
    - Follow ["Functional Core, Imperative Shell"](https://www.destroyallsoftware.com/screencasts/catalog/functional-core-imperative-shell).

      Keep the core logic pure, i.e immutable and stateless (or minimal state).

      Move stateful stuff (android UI interactions, database interactions) to the edge of your app. This would be on the "outside". It should utilize your core logic, which is now easy to test (since it's separated from stateful things).
    - Check out the tools of the trade slide! Use: Type inference (`var`), Switch expressions, Generics, Sealed classes/interfaces.
    - Use algebraic data types (sealed interface with records) to make illegal states unrepresentable.


Please also see git conventions in [GIT.md](./GIT.md)!

# Project Structure

Put full on data structures/classes inside `com.example.evently.data.model` (e.g `Event`).

Put activity groups in `com.example.evently.ui.<activity_name>` (e.g all activity/fragment stuff for Entrant screens goes into `com.example.evently.ui.entrant`)

Put database classes inside `com.example.evently.data`

Put generic helper types inside `com.example.evently.data.abstract`

Put utility classes and global constants inside `com.example.evently.utils`
