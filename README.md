# Memory Leak Repro

To reproduce, run `clojure -A:dev` and then call `user/go` from a repl. This starts a server on port 9000.

Using `wrk` to hit the server, you should see memory usage increase until the server runs out of memory. E.g.,

```
wrk -t4 -c50 -d10m -R80 --latency http://localhost:9000
```

