(define (domain vms_domain)
    (:requirements :adl :negative-preconditions :universal-preconditions)
    (:types human intent good_vibe)
    (:constants
        enter_email show_menu get_coffee get_wifi_info get_company_info leave - intent)
    (:predicates
        (is_interested ?h - human); Human is interested.
        (can_be_engaged ?h - human); Human is at good distance to engage them.
        (engaged_with ?h - human); We are engaging the human..
        (is_leaving ?h - human); Human is leaving.
        (was_greeted ?h - human); Human has been greeted by the robot.
        (was_goodbyed ?h - human); Human has been goodbyed by the robot.
        (was_checked_in ?h - human); Human has been checked in by the robot.
        (does_not_have_qrcode ?h - human)
        (wants ?h - human ?i - intent)
        (knows_intents ?h - human)
        (looking_alive)
        (provided_feedback ?h - human)
        (was_attracted ?h - human)
    )
    (:action attract
        :parameters (?h - human)
        :precondition (and (not (was_attracted ?h)) (not (is_leaving ?h)))
        :effect (and (was_attracted ?h) (can_be_engaged ?h) (engaged_with ?h))
    )
    (:action collect_feedback
        :parameters (?h - human)
        :precondition (and (engaged_with ?h) (was_checked_in ?h))
        :effect (provided_feedback ?h)
    )
    (:action idle
        :parameters ()
        :precondition ()
        :effect (looking_alive)
    )
    (:action enter_email
        :parameters (?h - human)
        :precondition (and (engaged_with ?h) (was_greeted ?h) (not (was_checked_in ?h)) (not (is_leaving ?h)) (does_not_have_qrcode ?h))
        :effect (was_checked_in ?h)
    )
    (:action goodbye
        :parameters (?h - human)
        :precondition (provided_feedback ?h)
        :effect (and (was_goodbyed ?h) (not (wants ?h leave)))
    )
    (:action greet
        :parameters (?h - human)
        :precondition (and (engaged_with ?h) (not (is_leaving ?h)))
        :effect (was_greeted ?h)
    )
    (:action make_coffee
        :parameters (?h - human)
        :precondition (and (engaged_with ?h) (wants ?h get_coffee) (not (is_leaving ?h)))
        :effect (not (wants ?h get_coffee))
    )
    (:action present_company
        :parameters (?h - human)
        :precondition (and (engaged_with ?h) (wants ?h get_company_info) (not (is_leaving ?h)))
        :effect (not (wants ?h get_company_info))
    )
    (:action scan
        :parameters (?h - human)
        :precondition (and (engaged_with ?h) (was_greeted ?h) (not (was_checked_in ?h)) (not (is_leaving ?h)) (not (does_not_have_qrcode ?h)))
        :effect (was_checked_in ?h)
    )
    (:action show_menu
        :parameters (?h - human)
        :precondition (and (engaged_with ?h) (was_checked_in ?h) (not (is_leaving ?h)))
        :effect (and (knows_intents ?h) (not (wants ?h show_menu)))
    )
    (:action show_wifi_info
        :parameters (?h - human)
        :precondition (and (engaged_with ?h) (wants ?h get_wifi_info) (not (is_leaving ?h)))
        :effect (not (wants ?h get_wifi_info))
    )
)
(define (problem sandbox_problem)
    (:domain vms_domain)
    (:requirements :adl :negative-preconditions :universal-preconditions)
    (:objects
        user - human;  Simple case: there is only one human, the user, and we reason around them
    )
    (:init)
    (:goal
        (and
            (forall (?i - intent) (not (wants user ?i)))
            (imply (is_leaving user) (was_goodbyed user))
            (imply (is_interested user) (knows_intents user))
            (imply (not (is_interested user)) (looking_alive))
        )
    )
)
