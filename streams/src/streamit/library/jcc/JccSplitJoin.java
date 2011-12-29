package streamit.library.jcc;

import streamit.library.SplitJoin;
import streamit.library.Stream;

public class JccSplitJoin extends JccCompositeFilter {

	protected final SplitJoin splitJoin;

	protected final JccSplitter splitter;

	protected final JccJoiner joiner;

	protected final JccStream[] filters;

	JccSplitJoin(SplitJoin splitJoin, JccSplitter splitter, JccJoiner joiner,
			JccStream[] filters) {
		this.splitJoin = splitJoin;
		this.splitter = splitter;
		this.joiner = joiner;
		this.filters = filters.clone();
	}

	@Override
	Stream getStreamIt() {
		return splitJoin;
	}

	@Override
	void setInChannel(JccChannel channel) {
		splitter.setInChannel(channel);
	}

	@Override
	void setOutChannel(JccChannel channel) {
		joiner.setOutChannel(channel);
	}

	@Override
	public void init() {
		splitter.init();
		joiner.init();

		for (int i = 0; i < filters.length; i++) {
			filters[i].init();
		}
	}

}
