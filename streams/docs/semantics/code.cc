// the base class for blocks
class Block {
  // this method is recognized by our compiler as representing an instance
  // of a given object.
  static final BlockDesc create(void* arg1 = 0, void* arg2 = 0, ...);
}

// the base class for splitters
class Splitter {
  static final SplitDesc create(void* arg1 = 0, void* arg2 = 0, ...);
}

// the base class for mergers
class Merger {
  static final MergeDesc create(void* arg1 = 0, void* arg2 = 0, ...);
}

// the base class for feedback loop initializers
class LoopInitializer {
  LoopInitDesc create(void* arg1 = 0, void* arg2 = 0, ...);
  // not sure about the arguments here, but there's some initializer function
  virtual void init(int delay, void** data) = 0;
}

class ParDesc;				// descriptor for parallel
class BlockDesc extends ParDesc;	// descriptor for 1-to-1

// pipe must take at least two arguments, and returns a pipeline of the
// enclosed block descriptions.
BlockDesc pipe(BlockDesc arg1 = 0, BlockDesc arg2 = 0, ...);

// for constructing parallel stuff when for contents of a parallel
ParDesc par(ParDesc arg1 = 0, ParDesc arg2 = 0, ...);

// the splitter construct
BlockDesc split(SplitDesc splitter, ParDesc body, MergeDesc merger)

// the feedback construct
BlockDesc loop(MergeDesc merger, BlockDesc block, int delay, LoopInitDesc init)

// the scoping for control constructs
BlockDesc controlGroup(BlockDesc block)

// the construct for going from a StreamDesc to a block
Block instantiate(BlockDesc desc);

// examples
BlockDesc stream1 = pipe(MySource.create(0,1,"init"),
			 MyFilter.create(),
			 MySink.create(1.0,'a'));

BlockDesc stream2 = split(DUPLICATOR.create(2),
		          ROUND_ROBIN.create(2),
			  par(MyFilter.create(), MyFilter.create()));

BlockDesc stream3 = loop(MyMerger.create(), pipe(stream1, stream2), 5, 
					    MyInitializer.create());

// for creating n copies of myfilter in parallel
ParDesc body = par();
for (int i=0; i<n; i++) {
    body = par(body, MyFilter.create(i));
}
BlockDesc stream4 = split(DUPLICATOR.create(n), body, ROUND_ROBIN.create(n));

// run the last example
instantiate(stream4).run();
