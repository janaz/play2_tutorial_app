package controllers;

import play.*;
import play.data.Form;
import play.mvc.*;
import models.Task;

import views.html.*;

public class Tasks extends Controller {

    static Form<Task> taskForm = Form.form(Task.class);


    public static Result tasks() {
        return ok(
                views.html.tasks.render(Task.all(), taskForm)
        );
    }

    public static Result newTask() {
        Form<Task> filledForm = taskForm.bindFromRequest();
        if(filledForm.hasErrors()) {
            return badRequest(
                    views.html.tasks.render(Task.all(), filledForm)
            );
        } else {
            Task.create(filledForm.get());
            return redirect(routes.Tasks.tasks());
        }
    }
    public static Result deleteTask(Long id) {
        Task.delete(id);
        return redirect(routes.Tasks.tasks());
    }


}
