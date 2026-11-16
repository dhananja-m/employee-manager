package com.example.employee_manager.controller;

import com.example.employee_manager.model.Employee;
import com.example.employee_manager.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    // Redirect root URL (http://localhost:3306) to /employees
    @GetMapping("/")
    public String home() {
        return "redirect:/employees";
    }

    // 1. READ: Show the registration form and the table of all employees
    @GetMapping("/employees")
    public String listEmployees(Model model) {
        if (!model.containsAttribute("employee")) {
            model.addAttribute("employee", new Employee());
        }
        model.addAttribute("employees", employeeService.getAllEmployees());
        return "employees";
    }

    // 2. CREATE: Save a new employee with validation and duplicate-email check
    @PostMapping("/employees")
    public String saveEmployee(@Valid @ModelAttribute("employee") Employee employee,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("employees", employeeService.getAllEmployees());
            return "employees";
        }

        try {
            employeeService.saveEmployee(employee);
            redirectAttributes.addFlashAttribute("successMessage", "Employee saved successfully!");
        } catch (IllegalArgumentException e) {
            // Reject duplicate email and display error under the email field
            result.rejectValue("email", "error.employee", e.getMessage());
            model.addAttribute("employees", employeeService.getAllEmployees());
            return "employees";
        }

        return "redirect:/employees";
    }

    // 3. EDIT FORM: Show the edit form pre-filled with the selected employee's data
    @GetMapping("/employees/{id}/edit")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Employee employee = employeeService.getEmployeeById(id);
        model.addAttribute("employee", employee);
        return "edit-employee";
    }

    // 4. UPDATE: Save the edited details
    @PostMapping("/employees/{id}/update")
    public String updateEmployee(@PathVariable("id") Long id,
                                 @Valid @ModelAttribute("employee") Employee employee,
                                 BindingResult result,
                                 RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            employee.setId(id);
            return "edit-employee";
        }

        try {
            employeeService.updateEmployee(id, employee);
            redirectAttributes.addFlashAttribute("successMessage", "Employee updated successfully!");
        } catch (IllegalArgumentException e) {
            result.rejectValue("email", "error.employee", e.getMessage());
            employee.setId(id);
            return "edit-employee";
        }

        return "redirect:/employees";
    }

    // 5. DELETE: Remove the employee
    @PostMapping("/employees/{id}/delete")
    public String deleteEmployee(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        employeeService.deleteEmployee(id);
        redirectAttributes.addFlashAttribute("successMessage", "Employee deleted successfully!");
        return "redirect:/employees";
    }
}

