/* 

Runtime System Interface, 1.0
9/13/01

The genearal idea is that the compiler outputs a set of functions,
including a top-level init function for the whole program.  This init
function calls child init functions in configuring the stream, and the
runtime system is informed of the stream's structure and the component
work functions via the library interface defined below.  Finally, when
the top-level stream definition calls "end_init", the runtime system
takes over and executes the stream that was defined.

THIS INTERFACE ASSUMES:

- no re-initialization

- the "canonical ordering" is exactly the lexicographical ordering of
the names given in the naming section of the interface spec.  For
ordering tapes, just take the order of the blocks and arrange each
input tape from left to right.

QUESTIONS:

- compiler will need to determine what exact numbers of items it can
reasonably demand on the tapes--this involves full analysis of the
init functions' peeking behavior.  Otherwise, it could have the right
push/pop ratios for the steady state, but couldn't go through with
data layout and stuff since it wouldn't know how many residual items
to expect from runtime system.

- what do you think about a traversal order for sub-streams that is
used to declare contents and minimum firing contents of tapes
implictly, without a naming convention or anything?  also used to go
from order of declaration to stream structure.  we could do something
hierarhical instead of flat, but I'm guessing that flat might be
easier inside the compiler.  We'll want to translate to hierarchical
in the runtime system, anyways.

*/

// for specifying stream types
typedef enum {
  filter,
  stream,
  split_join,
  feedback_loop
} stream_struct;

// for specifying split-join types.  will also want a mechanism to
// specify weighted round-robin's.
typedef enum {
  round_robin,
  null,
  duplicate,
  combine
} splitjoin_type;

// portal construct
typedef struct {
} portal;

// message construct
typedef struct {
  // parameters of message
  void* params;
} message;

// for specifying a list of discrete latencies
typedef struct _latency_list {
  int val;
  struct _latency_list *next;
} latency_list;

// for specifying a range of latencies.  will want special value or
// special type to signify best-effort delivery, as well as for
// specifying only a maximum time, instead of a range.
typedef struct {
  int min_val;
  int max_val;
} latency_range;

// for specifying a latency
typedef union {
  latency_list list;
  latency_range range;
} latency;

/* Variables for a given hierarchical unit are held in the <locals>
   construct.  This includes fields of an enclosing class, and local
   variables in the init function that are visible to nested structures. 
*/
typedef struct _locals {
  struct _locals *parent;
  int size;
  void* contents;
} locals;

// the internal data representation of a black box
typedef struct {
  void* contents;
} black_box_state;

// represents the contents of a tape
typedef struct {
  int num_items;
  void* contents;
} tape;

// the data representation for a hierarchical black box - state of
// tapes and state of component boxes.  The tapes and boxes are listed
// in the canonical ordering of the components.
typedef struct {
  black_box_state** state;
  tape** tape;
} hierarchical_state;

/*****************************************************************/
/*****************************************************************/

// BUILDER FUNCTIONS.  These should appear in init functions.

/*****************************************************************/
/*****************************************************************/

/*****************************************************************/
// builder functions - HIERARCHICAL UNITS ONLY (those that contain
// other black-boxes)
/*****************************************************************/

// for allocating local variables of size <size> of a stream construct
// that has other components.  the allocation of specific variables to
// a slot in the locals is done by the compiler.
locals* alloc_locals(int size);

/*****************************************************************/
// builder functions - GENERAL
/*****************************************************************/

// to call when entering and leaving an init function for a structure
// of type <s>.
void begin_init(stream_struct s);
void end_init(stream_struct s);

// to register current structure and component function <f> as a
// receiver for portal <p>.  the compiler will enforce the invariant
// that <p> is intended to deliver messages to objects implementing
// <f>.  in some cases a structure will register many functions with a
// given portal, depending on the type of the portal; in this case,
// the functions are numbered by <n> in both the receiver and sender.
// The latency ranges <l1> and <l2> represent the positions on the
// input and output tape, respectively, that a message from the given
// portal might be consumed during an execution step.
void register_receiver(portal p, void* f, int n, 
		       latency_range l1, 
		       latency_range l2);

// to register the current structure as a sender to portal <p> with
// possible latencies <l1> and <l2>.  These latencies are in terms of
// the positions on the structure's input and output tapes,
// respectively, at which the message should be delivered.  The
// positions are measured relative to the first item that the
// structure pops and pushes, respectively, on each invocation.
void register_sender(portal p, latency l1, latency l2);

/*****************************************************************/
// builder functions - SPLIT-JOIN and FEEDBACK-LOOP
/*****************************************************************/

// specify the splitter for the current structure
void set_splitter(splitjoin_type splitter);

// specify the joiner for the current structure
void set_joiner(splitjoin_type joiner);

/*****************************************************************/
// builder functions - FEEDBACK-LOOP ONLY
/*****************************************************************/

// markers for the beginning and end of the body of the feebdack loop
void start_body();
void end_body();

// markers for the beginning and end of the loop of the feebdack loop
void start_loop();
void end_loop();

// sets the delay of the feedback loop
void set_delay(int delay);

// sets pointer to the initPath function for the current structure
void set_init_path(void*(*init_path)(int));

/*****************************************************************/
// builder functions - BLACK BOXES ONLY
/*****************************************************************/

// these functions should be called before and after starting to
// enumerate the alternate, more fine-grained contents of the black
// box that the runtime system can use for precise message delivery.
void start_breakdown();
void end_breakdown();

// the next three functions must be called from within a black box to
// set the I/O ratios

// how many items are peeked per invocation of the black box
// (including the items that are popped--i.e. if a filter peeks 3 and
// pops 2, then it looks at 3, not 5.)
void set_peek(int i);

// how many items are popped per invocation
void set_pop(int i);

// how many items are pushed per invocation
void set_push(int i);

// to specify the steady-state work function of the current unit.
// input_items holds sufficient numbers of input items; output_items
// should be used to write the output of the step.
void set_work(black_box_state*(*work_function)(
		       black_box_state*,
		       void* input_items,
		       void* output_items));

// given the contents of the tapes and the internal states of the
// component black boxes (both given in the canonical order), "encode
// function" returns a single black-box-state for the current
// structure.
void encode_data(black_box_state*(*encode_function)(
			hierarchical_state* components));

// given the state of the black box of the current structure, "decode
// function" returns the states of the tapes and the component black
// boxes, specified in the canonical order.
void decode_data(hierarchical_state*(*decode_function)(
			black_box_state* state));

/*****************************************************************/
// builder functions - BLACK BOXES CONTAINING OTHER BLACK BOXES ONLY
/*****************************************************************/

// <tapes> specifies how many items are needed on each component tape
// to enter steady state.  there should be _exactly_ this many items
// when entering the steady state.  the tapes are specified in the
// canonical depth-first ordering of the components.
void set_steady_state_ratios(int* tapes);

/*****************************************************************/
// builder functions - BLACK BOXES -NOT- CONTAINING OTHER BLACK BOXES ONLY
/*****************************************************************/

// if the structure is a leaf-level black box (one that can not be
// decomposed into a smaller network of black boxes), then this
// end_init function should be called to give the runtime system the
// black-box structure following its initialization
void black_box_end_init(stream_struct s, black_box_state *bbs);

/*****************************************************************/
// portal sending functions - BLACK BOXES ONLY
/*****************************************************************/

// for a black box to enqueue a message <m> to function number <n> of
// portal <p> from within its work function.
void send_message(portal p, message m, int n);


