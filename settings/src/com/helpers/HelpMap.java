/*
Copyright (C) Petr Cada and Tomas Jedrzejek
This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

package com.helpers;


import com.spirit.R;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HelpMap {

    public static final Map<Integer, Integer> HELPMAP = createMap();

    private static Map<Integer, Integer> createMap() {
        Map<Integer, Integer> result = new HashMap<Integer, Integer>();

        result.put(R.id.position_select_id,                 R.string.st_26a7059fe05e155afb53ea22bc6a80d3);
        result.put(R.id.mix_select_id,                      R.string.st_a030379814acf4a9efd5735573f71718);
        result.put(R.id.receiver_select_id,                 R.string.st_06edb8aaa795fb406535ad1e42f7ddd9);
        result.put(R.id.cyclic_servo_reverse_select_id,     R.string.st_916898ebed3d1dbcb55c93994fec7ee1);
        result.put(R.id.flight_style_select_id,             R.string.st_9227b10a9b8a8dedbd04953e79838429);

        result.put(R.id.stabi_pitch,                        R.string.st_e26228953ce397eb25e87bcff3607d0e);
        result.put(R.id.stabi_ctrldir,                      R.string.st_1bb89987a641483fa0a25daa7e52d27c);
        result.put(R.id.stabi_fbmode,                       R.string.st_64d671d1921bac26046cf0d04da37ff1);
        result.put(R.id.function_select_id,                 R.string.st_1f82ad83632091d590ffe4a715dc0753);
        result.put(R.id.stabi_stick,                        R.string.st_bd22e71854e51838095b67f8c1d8441d);

        result.put(R.id.x_cyclic,                           R.string.st_40ac2993f4b9439ae49d61f0521b121d);
        result.put(R.id.z_rudder,                           R.string.st_466a542ef886802f5bbc2c23bc91fd06);
        result.put(R.id.gyro_gain,                          R.string.st_e1ef2f3a7b6fa63d68cb2b9b2e72a571);

        result.put(R.id.x_pitch_reverse,                    R.string.st_95e83a5f5c4f175cfefe9b06620fc385);
        result.put(R.id.y_roll_reverse,                     R.string.st_972307e460ecd3df8303f72849faaf36);
        result.put(R.id.z_yaw_reverse,                      R.string.st_6008eeff8fc4acff6e4ac6c6236c392e);

        result.put(R.id.x_pitch_rates,                      R.string.st_89aa34aff73db8d200536bf76779270b);
        result.put(R.id.z_yaw_rates,                        R.string.st_85767e852871648bc2db9dd4b052cf63);

        result.put(R.id.cyclicff,                           R.string.st_6c27918077d556edb488f0516b5965b6);
        result.put(R.id.cyclic_phase,                       R.string.st_cdf89e8bd20739bdc69e8c3bd7e1ae17);
        result.put(R.id.e_filter,                           R.string.st_45b61e5018b2249985746acf8e3424b3);
        result.put(R.id.geom_6deg,                          R.string.st_ce24a87287c6bcdc3c22b0432e7b7d87);
        result.put(R.id.piro_opt,                           R.string.st_91bffbd63276730c22ff5dc509529fa7);
        result.put(R.id.pirouette_const,                    R.string.st_a09dcd0b0fd2a8ed24f9d8a81b66668f);
        result.put(R.id.pitchup,                            R.string.st_28953f535f7d4bbe0b04ab8ffcbf3fde);
        result.put(R.id.rudder_delay,                       R.string.st_88ffaae209a942ffdcbf08d25ada6106);
        result.put(R.id.rudder_stop,                        R.string.st_050eaf6851d88f98084291c19b591a0d);
        result.put(R.id.rudder_revomix,                     R.string.st_f879dceafa5b8c110b5e108f25dd1b83);
        result.put(R.id.signal_processing,                  R.string.st_9a77db935bc1b64ee6402e6c8d019d44);
        result.put(R.id.stick_db,                           R.string.st_02282a0a0af7a569d19732b906395ed5);

        result.put(R.id.rudder_limit_min,                   R.string.st_3042744980d10a21a6e46a5834af02b1);
        result.put(R.id.rudder_limit_max,                   R.string.st_399fe255b29e9f87fd15daede396fdd7);

        result.put(R.id.cyclic_ring_ail_ele,                R.string.st_bef7e9faa6f15e367c6095280b3d5161);
        result.put(R.id.cyclic_ring_pitch,                  R.string.st_228ef0c717ed4671ff6c7d148917aeb2);

        result.put(R.id.governor_mode_select_id,            R.string.st_f91755e704b48300b6ed7744f871e754);
        result.put(R.id.governor_thr_min,                   R.string.st_06685249c2d7cce3e12bc2c3b3372506);
        result.put(R.id.governor_thr_max,                   R.string.st_599f538db3259fc1372f51277d9e17bc);
        result.put(R.id.governor_divider,                   R.string.st_cce3d39e3431c2bd38291a913943e2d8);
        result.put(R.id.governor_ratio,                     R.string.st_87bf01d68d2cab530a294799a9a02329);
        result.put(R.id.governor_rpm_max,                   R.string.st_4dcc47e798bbebbefc73502c4bd92f6d);
        result.put(R.id.governor_pgain,                     R.string.st_bc7ae3d3a122bdc0a016e266aafaa9cd);
        result.put(R.id.governor_igain,                     R.string.st_c2bdfc419aa48d9b2ce4e42ee0308247);


        return Collections.unmodifiableMap(result);
    }
}
