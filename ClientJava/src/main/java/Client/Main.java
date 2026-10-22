package Client;

import Client.controller.ControllerView;
import Client.view.GUI.ViewClientEncodage;

public class Main {

    public static void  main(String argv[] )
    {
        if(argv.length<2)
        {
            System.out.println("Utilisation : java Main <IP> <PORT>");
            return;
        }

        System.out.println(argv[0]);
        System.out.println(argv[1]);
        ViewClientEncodage view = new ViewClientEncodage();

        ControllerView controller = new ControllerView(view, argv[0], Integer.parseInt(argv[1]));
        view.setController(controller);
        controller.run();
    }
}
