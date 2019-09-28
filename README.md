[![Version](https://img.shields.io/github/v/release/mejlholm/openapi-map)](https://github.com/mejlholm/openapi-map/releases/latest)
[![License](https://img.shields.io/github/license/mejlholm/openapi-map?)](https://www.gnu.org/licenses/gpl-3.0.html)
[![Build Status](https://cloud.drone.io/api/badges/mejlholm/openapi-map/status.svg)](https://cloud.drone.io/mejlholm/openapi-map)

# OpenAPI Map

OpenAPI map is a kubernetes application that looks for applications exposing OpenAPI inside your cluster and aggregates these into a single webpage for a quick overview of you services.

Built on [Quarkus.io](https://quarkus.io/) and a bit of jquery, 

## Getting started

Adjust the namespace in the deploy/*.yaml files
~~~Shell
kubectl apply -f deploy
~~~