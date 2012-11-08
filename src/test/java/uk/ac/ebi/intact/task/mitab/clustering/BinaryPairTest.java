package uk.ac.ebi.intact.task.mitab.clustering;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tester of BinaryPair
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>06/02/12</pre>
 */

public class BinaryPairTest {

    @Test
    public void compare_two_interactors_same_orders(){
        BinaryPair pair1 = new BinaryPair("test1", "test2");
        BinaryPair pair2 = new BinaryPair("test1", "test2");

        Assert.assertEquals(pair1, pair2);
        Assert.assertEquals(0, pair1.compareTo(pair2));
    }

    @Test
    public void compare_two_interactors_different_orders(){
        BinaryPair pair1 = new BinaryPair("test1", "test2");
        BinaryPair pair2 = new BinaryPair("test2", "test1");

        Assert.assertEquals(pair1, pair2);
        Assert.assertEquals(0, pair1.compareTo(pair2));
    }

    @Test
    public void compare_different_interactors_pair1_before_pair2(){
        BinaryPair pair1 = new BinaryPair("test1", "test2");
        BinaryPair pair2 = new BinaryPair("test3", "test1");

        Assert.assertNotSame(pair1, pair2);
        Assert.assertEquals(-1, pair1.compareTo(pair2));
    }

    @Test
    public void compare_interactor_null_pair2_before_pair1(){
        BinaryPair pair1 = new BinaryPair("test1", null);
        BinaryPair pair2 = new BinaryPair("test3", "test1");

        Assert.assertNotSame(pair1, pair2);
        Assert.assertEquals(1, pair1.compareTo(pair2));
    }
}
