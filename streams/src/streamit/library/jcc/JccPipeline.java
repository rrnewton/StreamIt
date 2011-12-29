package streamit.library.jcc;

import streamit.library.Pipeline;
import streamit.library.Stream;

public class JccPipeline extends JccCompositeFilter {

	protected final Pipeline pipeline;

	protected final JccStream[] filters;

	JccPipeline(Pipeline pipeline, JccStream[] filters) {
		this.pipeline = pipeline;
		this.filters = filters.clone();
	}

	@Override
	Stream getStreamIt() {
		return pipeline;
	}

	@Override
	void setInChannel(JccChannel channel) {
		filters[0].setInChannel(channel);
	}

	@Override
	void setOutChannel(JccChannel channel) {
		filters[filters.length - 1].setOutChannel(channel);
	}

	@Override
	public void init() {
		for (int i = 0; i < filters.length; i++) {
			filters[i].init();
		}
	}

}
