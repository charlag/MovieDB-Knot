# Knot sample app

This apps strives to demonstrate modern techniques to writing modular, highly testable code. It uses approach similar to
Redux and Redux-Observable: it uses reducers of type `(S, E) -> S` for state chagnes and it uses 'epics' for side-effects.

Each epic is like a small user-story. They take event and the state stream as the input and they return stream of events
will be dispatched to the reducers and other epics.

This app also uses Dagger 2 for DI but it doesn't use it for injecting Epics which could be beneficial.

As this is a sample app it makes some simplifications:
 * Views shouldn't know about the whole app state, some mediator should adapt the state for the view.
 * In the same way views shouldn't know about Events.
 * Same models are used for the logic layer and for the view layer
 * Functionality like date filtering doesn't work like it should
