package jobs;

import io.mangoo.annotations.Run;

public class InfoJobEveryDay {
    @Run(at = "Every 3d")
    public void execute(){
        // Nothing to do here
    }

    public void foo() {
    }

    @SuppressWarnings("ALL")
    public void bar() {
    }
}