package jobs;

import io.mangoo.annotations.Run;

public class InfoJobEveryDay {
    @Run(at = "Every 3d")
    public void execute(){
        //Do nothing for now
    }

    public void foo() {
    }

    @SuppressWarnings("ALL")
    public void bar() {
    }
}