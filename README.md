# Memory Leak Repro

To reproduce, start the server with `thomascothran.reitit-repro.core/start-server`.

There are two endpoints: '/test' and '/test2'. The former uses Reitit's default json serialization, the second uses `clojure.data.json` to convert the map to a string. They serve the same payload.

Using `wrk` to hit the endpoints, you should see memory usage with the `test` endpoint increase until it runs out of memory. E.g.,

```
wrk -t4 -c50 -d10m -R80 --latency http://localhost:9000/test
```

The `/test2` endpoint, by contrast, does not run out of memory.

