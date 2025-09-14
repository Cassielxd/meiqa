
import BasicLayout from "@/components/main";

const meta = {};

const pre = "user_";

export default {
    path: "/tenant/user",
    name: "user",
    header: "user",
    redirect: {
        name: `${pre}list`
    },
    meta,
    component: BasicLayout,
    children: [
        {
            path: "list",
            name: `${pre}list`,
            meta: {
                title: "用户管理"
            },
            component: () => import("@/pages/user/list/index")
        },
        {
            path: "group",
            name: `${pre}group`,
            meta: {
                footer: true,
                title: "用户分组"
            },
            component: () => import("@/pages/user/group/index")
        },
        {
            path: "label",
            name: `${pre}label`,
            meta: {
                footer: true,
                title: "用户标签"
            },
            component: () => import("@/pages/user/label/index")
        },
    ]
};
