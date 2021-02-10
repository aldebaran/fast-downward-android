(define (problem sandbox_problem)
    (:domain vms_domain)
    (:requirements :adl :negative-preconditions :universal-preconditions)
    (:objects
        human1 human2 human3 human4 human5 - human
    )
    (:init
        (is_interested human1)
    )
    (:goal
        (and
            (imply (not (exists (?h - human) (is_interested ?h))) (looking_alive))
            (forall (?h - human)
                (and
                    ; Get to know what interested people want.
                    (imply
                        (and (is_interested ?h) (not (is_leaving ?h)))
                        (knows_intents ?h))

                    ; Make sure what they want is satisfied.
                    (not (exists (?i - intent) (wants ?h ?i)))

                    ; Get some feedback when leaving, as long as we can engage them.
                    (imply
                        (and (is_leaving ?h) (can_be_engaged ?h))
                        (provided_feedback ?h)
                    )

                    ; Say goodbye when people leave.
                    (imply
                        (is_leaving ?h)
                        (was_goodbyed ?h)
                    )
                )
            )
        )
    )
)