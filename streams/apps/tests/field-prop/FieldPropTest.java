/**
 * Simple application used for regression testing of
 * field propagation.
 * $Id: FieldPropTest.java,v 1.1 2002-06-18 17:09:32 aalamb Exp $
 **/
import streamit.*;

class FieldPropTest extends StreamIt {
  static public void main (String [] t)
    {
        FieldPropTest test = new FieldPropTest ();
        test.run (t);
    }

    public void init ()
    {
	add (new DataSource());
	add (new OneToOne());
	add (new OneToTwo());
	add (new DataSink());
    }
}

class DataSource extends Filter {
    int x;
    public void init() {
	// pushes 1 item onto the tape each iteration
	output = new Channel(Integer.TYPE, 1); 
	this.x = 0;
    }
    public void work() {
	output.pushInt(this.x);
	this.x++;
    }
}

class DataSink extends Filter {
    public void init() {
	// pops 1 item from the tape each iteration
	input = new Channel(Integer.TYPE, 1); 
    }

    public void work() {
	System.out.println(input.popInt());
    }
}





class OneToOne extends Filter {
    static final int F = 6;
    public void init() {
	input = new Channel(Integer.TYPE, 1);
	output= new Channel(Integer.TYPE, 1);
    }

    public void work() {
	output.pushInt(input.popInt());
    }
}


class OneToTwo extends Filter {
  public void init() {
    input = new Channel(Integer.TYPE, 1);
    output= new Channel(Integer.TYPE, 2);
  }
  
  public void work() {
    int temp_value = input.popInt();
    output.pushInt(temp_value);
    output.pushInt(OneToOne.F);
  }
}
