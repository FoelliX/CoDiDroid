import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.Test;

import de.foellix.aql.Log;
import de.foellix.aql.operator.horndroid.CheckOperator;

public class CheckOperatorTest {
	@Test
	void test() {
		Log.setLogLevel(Log.DEBUG_DETAILED);

		boolean noException = true;
		try {
			final File testTCFile = new File("test/AnswerTC.xml");
			final File testHDFile = new File("test/AnswerHD.xml");
			new CheckOperator(testTCFile, testHDFile);
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
	}
}
