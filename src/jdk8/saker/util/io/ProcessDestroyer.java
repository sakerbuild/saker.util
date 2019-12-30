package saker.util.io;

class ProcessDestroyer {
	private ProcessDestroyer() {
		throw new UnsupportedOperationException();
	}

	public static void destroyProcessAndPossiblyChildren(Process p) {
		p.destroy();
	}
}
