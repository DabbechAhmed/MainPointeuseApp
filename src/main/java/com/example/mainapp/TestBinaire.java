package com.example.mainapp;

        import com.example.mainapp.model.*;
        import com.example.mainapp.enums.Status;
        import com.example.mainapp.service.PersistenceManager;
        import java.time.DayOfWeek;
        import java.time.LocalTime;

        public class TestBinaire {

            public static void main(String[] args) {

                System.out.println("=== 1. PRÉPARATION DES DONNÉES ===");

                Company maCompagnie = new Company("Polytech Tours");

                // ==========================================
                // CRÉATION DES DÉPARTEMENTS
                // ==========================================
                Department deptIT = new Department("Informatique");
                Department deptRH = new Department("Ressources Humaines");
                Department deptAccounting = new Department("Comptabilité");
                Department deptLogistics = new Department("Logistique");

                // ==========================================
                // CRÉATION DES EMPLOYÉS - DÉPARTEMENT IT
                // ==========================================
                Employee emp1 = new Employee(deptIT, "M'SADAA", "Youssef", Status.EMP);
                Employee emp2 = new Employee(deptIT, "DEBBACH", "Ahmed", Status.EMP);
                Employee emp3 = new Employee(deptIT, "RIANI", "Youssef", Status.EMP);

                // Schedule différents pour les employés IT
                configurerScheduleIT(emp1);  // 08:00 - 17:00
                configurerScheduleIT(emp2);  // 09:00 - 18:00
                configurerScheduleIT(emp3);  // 07:30 - 16:30

                // ==========================================
                // CRÉATION DES EMPLOYÉS - DÉPARTEMENT RH
                // ==========================================
                Employee emp4 = new Employee(deptRH, "BEN ABDA", "Mohamed Yassine", Status.HRR);
                Employee emp5 = new Employee(deptRH, "SOPHIE", "Laurent", Status.EMP);

                configurerScheduleRH(emp4);  // 08:30 - 17:30
                configurerScheduleRH(emp5);  // 09:00 - 17:00

                // ==========================================
                // CRÉATION DES EMPLOYÉS - DÉPARTEMENT COMPTABILITÉ
                // ==========================================
                Employee emp6 = new Employee(deptAccounting, "MARTIN", "Philippe", Status.EMP);
                Employee emp7 = new Employee(deptAccounting, "DURAND", "Marie", Status.EMP);

                configurerScheduleAccounting(emp6);  // 08:00 - 16:30
                configurerScheduleAccounting(emp7);  // 08:30 - 17:00

                // ==========================================
                // CRÉATION DES EMPLOYÉS - DÉPARTEMENT LOGISTIQUE
                // ==========================================
                Employee emp8 = new Employee(deptLogistics, "BERNARD", "Pascal", Status.EMP);
                Employee emp9 = new Employee(deptLogistics, "THOMAS", "Nicolas", Status.EMP);
                Employee emp10 = new Employee(deptLogistics, "EL YAHYAOUI", "Youssef", Status.EMP);

                configurerScheduleLogistics(emp8);   // 06:00 - 14:00
                configurerScheduleLogistics(emp9);   // 14:00 - 22:00
                configurerScheduleLogistics(emp10);  // 07:00 - 15:00

                // ==========================================
                // ÉTABLISSEMENT DES LIENS
                // ==========================================
                maCompagnie.addDepartment(deptIT);
                maCompagnie.addDepartment(deptRH);
                maCompagnie.addDepartment(deptAccounting);
                maCompagnie.addDepartment(deptLogistics);

                // Ajouter tous les employés à la compagnie
                Employee[] tousEmployes = {emp1, emp2, emp3, emp4, emp5, emp6, emp7, emp8, emp9, emp10};
                for (Employee emp : tousEmployes) {
                    maCompagnie.addEmployee(emp);
                }

                // Ajouter les employés à leurs départements respectifs
                deptIT.addEmployee(emp1);
                deptIT.addEmployee(emp2);
                deptIT.addEmployee(emp3);

                deptRH.addEmployee(emp4);
                deptRH.addEmployee(emp5);

                deptAccounting.addEmployee(emp6);
                deptAccounting.addEmployee(emp7);

                deptLogistics.addEmployee(emp8);
                deptLogistics.addEmployee(emp9);
                deptLogistics.addEmployee(emp10);

                System.out.println("✅ Données générées avec succès (10 employés répartis sur 4 départements).");

                System.out.println("\n=== 2. TEST DE SAUVEGARDE ===");
                PersistenceManager.saveData(maCompagnie);

                System.out.println("\n=== 3. TEST DE CHARGEMENT ===");
                Company compagnieChargee = PersistenceManager.loadData();

                System.out.println("\n=== 4. VÉRIFICATION DE L'INTÉGRITÉ ===");
                if (compagnieChargee != null && !compagnieChargee.getEmployees().isEmpty()) {

                    System.out.println("🏢 Entreprise : " + compagnieChargee.getName());
                    System.out.println("🏭 Nombre de départements : " + compagnieChargee.getDepartments().size());

                    // ✅ NOUVEAU : On affiche les départements et leurs ID pour vérifier
                    for(Department d : compagnieChargee.getDepartments()) {
                        System.out.println("   -> " + d.getName() + " (ID: " + d.getId() + ")");
                    }

                    System.out.println("👥 Nombre d'employés récupérés : " + compagnieChargee.getEmployees().size());
                    System.out.println("-------------------------------------------------");

                    for (Employee recup : compagnieChargee.getEmployees()) {
                        // ✅ NOUVEAU : On affiche aussi l'ID du département lié à l'employé
                        String infoDept = (recup.getDepartment() != null)
                                ? recup.getDepartment().getName() + " [" + recup.getDepartment().getId() + "]"
                                : "Aucun";

                        System.out.println("👤 " + recup.getName() + " " + recup.getSurname() +
                                "\n    | Dpt: " + infoDept +
                                "\n    | Status: " + recup.getStatus() +
                                "\n    | Horaires Lundi: " + recup.getSchedule().getHorairePourJour(DayOfWeek.MONDAY));
                    }
                    System.out.println("-------------------------------------------------");
                }
            }

            // ==========================================
            // MÉTHODES AUXILIAIRES - CONFIGURATION DES SCHEDULES
            // ==========================================

            private static void configurerScheduleIT(Employee emp) {
                Schedule schedule = emp.getSchedule();
                TimeSlot slot;

                if (emp.getName().equals("M'SADAA")) {
                    slot = new TimeSlot(LocalTime.of(8, 0), LocalTime.of(17, 0));
                } else if (emp.getName().equals("DEBBACH")) {
                    slot = new TimeSlot(LocalTime.of(9, 0), LocalTime.of(18, 0));
                } else {
                    slot = new TimeSlot(LocalTime.of(7, 30), LocalTime.of(16, 30));
                }

                for (DayOfWeek day : DayOfWeek.values()) {
                    schedule.definirJournee(day, slot);
                }
            }

            private static void configurerScheduleRH(Employee emp) {
                Schedule schedule = emp.getSchedule();
                TimeSlot slot;

                if (emp.getStatus() == Status.HRR) {
                    slot = new TimeSlot(LocalTime.of(8, 30), LocalTime.of(17, 30));
                } else {
                    slot = new TimeSlot(LocalTime.of(9, 0), LocalTime.of(17, 0));
                }

                for (DayOfWeek day : DayOfWeek.values()) {
                    schedule.definirJournee(day, slot);
                }
            }

            private static void configurerScheduleAccounting(Employee emp) {
                Schedule schedule = emp.getSchedule();
                TimeSlot slot;

                if (emp.getName().equals("MARTIN")) {
                    slot = new TimeSlot(LocalTime.of(8, 0), LocalTime.of(16, 30));
                } else {
                    slot = new TimeSlot(LocalTime.of(8, 30), LocalTime.of(17, 0));
                }

                for (DayOfWeek day : DayOfWeek.values()) {
                    schedule.definirJournee(day, slot);
                }
            }

            private static void configurerScheduleLogistics(Employee emp) {
                Schedule schedule = emp.getSchedule();
                TimeSlot slot;

                if (emp.getName().equals("BERNARD")) {
                    slot = new TimeSlot(LocalTime.of(6, 0), LocalTime.of(14, 0));
                } else if (emp.getName().equals("THOMAS")) {
                    slot = new TimeSlot(LocalTime.of(14, 0), LocalTime.of(22, 0));
                } else {
                    slot = new TimeSlot(LocalTime.of(7, 0), LocalTime.of(15, 0));
                }

                for (DayOfWeek day : DayOfWeek.values()) {
                    schedule.definirJournee(day, slot);
                }
            }
        }