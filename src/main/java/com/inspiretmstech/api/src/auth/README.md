# Creating New Methods

## Create Method Package

## Implement Provider in Authentication Filter

In `AuthenticationFilter`, add the new provider in `List<AuthenticationProvider<?>> providers`. 
Precedent/ordering does matter; the earlier in the list, the higher precedent will be applied.
Note: the first supported provider is the one that will be used.
