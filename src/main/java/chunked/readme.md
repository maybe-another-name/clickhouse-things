# notes on writing many things

current premise (based on notes in 'public-stuff'):
* will not use buffer tables, as apparently the async stuff is a replacement for that
* will not be using the 'no-wait' (fire-and-forget), as that is not appropriate for the use-case

## options:
* may ask team to raise some of the default configs (see benchmark #2 info)

# approach
* to get around timeout issues (since the wait could still trigger them, even if successful), will combine client-side batching with async writes
* since the wait is synchronous, could use separate threads for each chunk (would probably want to tailor configs according to this)

## chunking/client-side batching

* will continue to use 'split' command to chunk the file
* will use separate thread for each 'chunked' file